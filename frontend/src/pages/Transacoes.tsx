import { useEffect, useMemo, useState } from "react";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { isAxiosError } from "axios";
import NavBar from "../components/NavBar";
import { listarContas } from "../api/contas";
import { listarCategorias } from "../api/categorias";
import {
  listarTransacoes,
  criarTransacao,
  excluirTransacao,
} from "../api/transacoes";
import { formatarMoeda, formatarData } from "../utils/formato";
import type { Conta } from "../types/conta";
import type { Categoria } from "../types/categoria";
import type { PageTransacoes } from "../types/transacao";
import type { ApiError } from "../types/api-error";

const TAMANHO_PAGINA = 20;

const transacaoSchema = z.object({
  descricao: z.string().min(1, "A descrição é obrigatória"),
  valor: z.coerce.number().positive("O valor deve ser maior que zero"),
  tipo: z.enum(["RECEITA", "DESPESA"]),
  contaId: z.coerce.number({ message: "Selecione uma conta" }),
  categoriaId: z.coerce.number({ message: "Selecione uma categoria" }),
  dataTransacao: z.string().min(1, "A data é obrigatória"),
});

type TransacaoFormInput = z.input<typeof transacaoSchema>;
type TransacaoForm = z.output<typeof transacaoSchema>;

function hoje(): string {
  const data = new Date();
  const ano = data.getFullYear();
  const mes = String(data.getMonth() + 1).padStart(2, "0");
  const dia = String(data.getDate()).padStart(2, "0");
  return `${ano}-${mes}-${dia}`;
}

export default function Transacoes() {
  const [contas, setContas] = useState<Conta[]>([]);
  const [categorias, setCategorias] = useState<Categoria[]>([]);

  const [pagina, setPagina] = useState<PageTransacoes | null>(null);
  const [numeroPagina, setNumeroPagina] = useState(0);
  const [carregandoLista, setCarregandoLista] = useState(true);
  const [erroLista, setErroLista] = useState<string | null>(null);

  const [filtroDataInicio, setFiltroDataInicio] = useState("");
  const [filtroDataFim, setFiltroDataFim] = useState("");
  const [filtroContaId, setFiltroContaId] = useState<string>("");
  const [filtroCategoriaId, setFiltroCategoriaId] = useState<string>("");

  const [erroForm, setErroForm] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    control,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<TransacaoFormInput, unknown, TransacaoForm>({
    resolver: zodResolver(transacaoSchema),
    defaultValues: {
      tipo: "DESPESA",
      dataTransacao: hoje(),
    },
  });

  const tipoSelecionado = watch("tipo");

  const categoriasFiltradas = useMemo(
    () => categorias.filter((categoria) => categoria.tipo === tipoSelecionado),
    [categorias, tipoSelecionado]
  );

  // Sempre que o tipo mudar, a categoria selecionada anteriormente pode não
  // pertencer mais à lista filtrada — limpamos para evitar enviar um
  // categoriaId de tipo divergente sem o usuário perceber.
  useEffect(() => {
    setValue("categoriaId", undefined as unknown as TransacaoFormInput["categoriaId"]);
  }, [tipoSelecionado, setValue]);

  // dados de apoio (contas e categorias pros selects), carregados uma vez
  useEffect(() => {
    async function carregarApoio() {
      try {
        const [contasResposta, categoriasResposta] = await Promise.all([
          listarContas(),
          listarCategorias(),
        ]);
        setContas(contasResposta);
        setCategorias(categoriasResposta);
      } catch {
        // erro aqui não impede a listagem de transações; o formulário
        // simplesmente fica sem opções nos selects
      }
    }
    carregarApoio();
  }, []);

  useEffect(() => {
    let cancelado = false;

    async function carregarTransacoes() {
      setCarregandoLista(true);
      setErroLista(null);
      try {
        const resposta = await listarTransacoes({
          dataInicio: filtroDataInicio || undefined,
          dataFim: filtroDataFim || undefined,
          contaId: filtroContaId ? Number(filtroContaId) : undefined,
          categoriaId: filtroCategoriaId ? Number(filtroCategoriaId) : undefined,
          page: numeroPagina,
          size: TAMANHO_PAGINA,
        });
        if (!cancelado) {
          setPagina(resposta);
        }
      } catch (error) {
        if (cancelado) return;
        if (isAxiosError<ApiError>(error) && error.response) {
          setErroLista(error.response.data.message);
        } else {
          setErroLista("Não foi possível carregar as transações.");
        }
      } finally {
        if (!cancelado) setCarregandoLista(false);
      }
    }

    carregarTransacoes();

    return () => {
      cancelado = true;
    };
  }, [filtroDataInicio, filtroDataFim, filtroContaId, filtroCategoriaId, numeroPagina]);

  function atualizarFiltro(setter: (valor: string) => void, valor: string) {
    setter(valor);
    setNumeroPagina(0);
  }

  async function recarregarListaAtual() {
    setCarregandoLista(true);
    setErroLista(null);
    try {
      const resposta = await listarTransacoes({
        dataInicio: filtroDataInicio || undefined,
        dataFim: filtroDataFim || undefined,
        contaId: filtroContaId ? Number(filtroContaId) : undefined,
        categoriaId: filtroCategoriaId ? Number(filtroCategoriaId) : undefined,
        page: numeroPagina,
        size: TAMANHO_PAGINA,
      });
      setPagina(resposta);
    } catch (error) {
      if (isAxiosError<ApiError>(error) && error.response) {
        setErroLista(error.response.data.message);
      } else {
        setErroLista("Não foi possível carregar as transações.");
      }
    } finally {
      setCarregandoLista(false);
    }
  }

  async function onSubmit(dados: TransacaoForm) {
    setErroForm(null);
    try {
      await criarTransacao(dados);
      reset({
        descricao: "",
        valor: undefined,
        tipo: dados.tipo,
        contaId: undefined,
        categoriaId: undefined,
        dataTransacao: hoje(),
      });
      setNumeroPagina(0);
      await recarregarListaAtual();
    } catch (error) {
      if (isAxiosError<ApiError>(error) && error.response) {
        setErroForm(error.response.data.message);
      } else {
        setErroForm("Não foi possível criar a transação. Tente novamente.");
      }
    }
  }

  async function handleExcluir(id: number) {
    const confirmado = window.confirm(
      "Tem certeza que deseja excluir esta transação?"
    );
    if (!confirmado) return;

    try {
      await excluirTransacao(id);
      await recarregarListaAtual();
    } catch (error) {
      if (isAxiosError<ApiError>(error) && error.response) {
        setErroLista(error.response.data.message);
      } else {
        setErroLista("Não foi possível excluir a transação.");
      }
    }
  }

  return (
    <div className="min-h-screen bg-paper">
      <NavBar />

      <main className="max-w-4xl mx-auto px-4 py-6 space-y-6">
        <h1 className="font-display text-2xl text-ledger">Transações</h1>

        <div className="bg-white border border-line rounded-lg shadow-sm p-6">
          <h2 className="text-sm font-medium text-ink mb-4">Nova transação</h2>

          {erroForm && (
            <div className="mb-4 rounded-md bg-danger/10 border border-danger/20 px-3 py-2 text-sm text-danger">
              {erroForm}
            </div>
          )}

          <form
            onSubmit={handleSubmit(onSubmit)}
            className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 items-start"
            noValidate
          >
            <div className="lg:col-span-2">
              <label htmlFor="descricao" className="block text-sm font-medium text-ink mb-1">
                Descrição
              </label>
              <input
                id="descricao"
                type="text"
                className="w-full rounded-md border border-line px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ledger"
                {...register("descricao")}
              />
              {errors.descricao && (
                <p className="mt-1 text-xs text-danger">{errors.descricao.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="valor" className="block text-sm font-medium text-ink mb-1">
                Valor
              </label>
              <input
                id="valor"
                type="number"
                step="0.01"
                min="0.01"
                placeholder="0,00"
                className="w-full rounded-md border border-line px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ledger"
                {...register("valor")}
              />
              {errors.valor && (
                <p className="mt-1 text-xs text-danger">{errors.valor.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="tipo" className="block text-sm font-medium text-ink mb-1">
                Tipo
              </label>
              <select
                id="tipo"
                className="w-full rounded-md border border-line px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ledger bg-white"
                {...register("tipo")}
              >
                <option value="DESPESA">Despesa</option>
                <option value="RECEITA">Receita</option>
              </select>
            </div>

            <div>
              <label htmlFor="contaId" className="block text-sm font-medium text-ink mb-1">
                Conta
              </label>
              <select
                id="contaId"
                className="w-full rounded-md border border-line px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ledger bg-white"
                defaultValue=""
                {...register("contaId")}
              >
                <option value="" disabled>
                  Selecione
                </option>
                {contas.map((conta) => (
                  <option key={conta.id} value={conta.id}>
                    {conta.nome}
                  </option>
                ))}
              </select>
              {errors.contaId && (
                <p className="mt-1 text-xs text-danger">{errors.contaId.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="categoriaId" className="block text-sm font-medium text-ink mb-1">
                Categoria
              </label>
              <Controller
                name="categoriaId"
                control={control}
                render={({ field }) => (
                  <select
                    id="categoriaId"
                    className="w-full rounded-md border border-line px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ledger bg-white"
                    value={(field.value as string | number | undefined) ?? ""}
                    onChange={(e) => field.onChange(e.target.value)}
                  >
                    <option value="" disabled>
                      Selecione
                    </option>
                    {categoriasFiltradas.map((categoria) => (
                      <option key={categoria.id} value={categoria.id}>
                        {categoria.nome}
                      </option>
                    ))}
                  </select>
                )}
              />
              {errors.categoriaId && (
                <p className="mt-1 text-xs text-danger">{errors.categoriaId.message}</p>
              )}
              {categoriasFiltradas.length === 0 && (
                <p className="mt-1 text-xs text-ink/60">
                  Nenhuma categoria de {tipoSelecionado === "RECEITA" ? "receita" : "despesa"} cadastrada ainda.
                </p>
              )}
            </div>

            <div>
              <label htmlFor="dataTransacao" className="block text-sm font-medium text-ink mb-1">
                Data
              </label>
              <input
                id="dataTransacao"
                type="date"
                className="w-full rounded-md border border-line px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ledger"
                {...register("dataTransacao")}
              />
              {errors.dataTransacao && (
                <p className="mt-1 text-xs text-danger">{errors.dataTransacao.message}</p>
              )}
            </div>

            <div className="sm:col-span-2 lg:col-span-3">
              <button
                type="submit"
                disabled={isSubmitting}
                className="rounded-md bg-ledger text-white px-4 py-2 text-sm font-medium hover:bg-ledger-light transition-colors disabled:opacity-60"
              >
                {isSubmitting ? "Salvando..." : "Adicionar transação"}
              </button>
            </div>
          </form>
        </div>

        <div className="bg-white border border-line rounded-lg shadow-sm p-4">
          <p className="text-sm font-medium text-ink mb-3">Filtros</p>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
            <div>
              <label htmlFor="filtroDataInicio" className="block text-xs text-ink/60 mb-1">
                De
              </label>
              <input
                id="filtroDataInicio"
                type="date"
                value={filtroDataInicio}
                onChange={(e) => atualizarFiltro(setFiltroDataInicio, e.target.value)}
                className="w-full rounded-md border border-line px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-ledger"
              />
            </div>
            <div>
              <label htmlFor="filtroDataFim" className="block text-xs text-ink/60 mb-1">
                Até
              </label>
              <input
                id="filtroDataFim"
                type="date"
                value={filtroDataFim}
                onChange={(e) => atualizarFiltro(setFiltroDataFim, e.target.value)}
                className="w-full rounded-md border border-line px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-ledger"
              />
            </div>
            <div>
              <label htmlFor="filtroConta" className="block text-xs text-ink/60 mb-1">
                Conta
              </label>
              <select
                id="filtroConta"
                value={filtroContaId}
                onChange={(e) => atualizarFiltro(setFiltroContaId, e.target.value)}
                className="w-full rounded-md border border-line px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-ledger bg-white"
              >
                <option value="">Todas as contas</option>
                {contas.map((conta) => (
                  <option key={conta.id} value={conta.id}>
                    {conta.nome}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label htmlFor="filtroCategoria" className="block text-xs text-ink/60 mb-1">
                Categoria
              </label>
              <select
                id="filtroCategoria"
                value={filtroCategoriaId}
                onChange={(e) => atualizarFiltro(setFiltroCategoriaId, e.target.value)}
                className="w-full rounded-md border border-line px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-ledger bg-white"
              >
                <option value="">Todas as categorias</option>
                {categorias.map((categoria) => (
                  <option key={categoria.id} value={categoria.id}>
                    {categoria.nome}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {erroLista && (
          <div className="rounded-md bg-danger/10 border border-danger/20 px-3 py-2 text-sm text-danger">
            {erroLista}
          </div>
        )}

        {carregandoLista ? (
          <div className="space-y-2">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-16 rounded-lg border border-line bg-white animate-pulse" />
            ))}
          </div>
        ) : (
          <>
            <div className="space-y-2">
              {pagina?.content.map((transacao) => (
                <div
                  key={transacao.id}
                  className="bg-white border border-line rounded-lg shadow-sm p-4 flex items-center justify-between gap-3"
                >
                  <div className="min-w-0">
                    <p className="text-sm font-medium text-ink truncate">
                      {transacao.descricao}
                    </p>
                    <p className="text-xs text-ink/60">
                      {transacao.categoriaNome} · {transacao.contaNome} ·{" "}
                      {formatarData(transacao.dataTransacao)}
                    </p>
                  </div>
                  <div className="flex items-center gap-3 shrink-0">
                    <p
                      className={`text-sm font-display ${
                        transacao.tipo === "RECEITA" ? "text-ledger" : "text-danger"
                      }`}
                    >
                      {transacao.tipo === "RECEITA" ? "+" : "-"}
                      {formatarMoeda(transacao.valor)}
                    </p>
                    <button
                      onClick={() => handleExcluir(transacao.id)}
                      className="text-xs text-danger hover:underline"
                    >
                      Excluir
                    </button>
                  </div>
                </div>
              ))}
              {pagina?.content.length === 0 && (
                <p className="text-sm text-ink/60">Nenhuma transação encontrada</p>
              )}
            </div>

            {pagina && pagina.totalPages > 1 && (
              <div className="flex items-center justify-center gap-4 pt-2">
                <button
                  onClick={() => setNumeroPagina((p) => Math.max(0, p - 1))}
                  disabled={pagina.number === 0}
                  className="text-sm text-ledger font-medium disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  Anterior
                </button>
                <span className="text-sm text-ink/60">
                  Página {pagina.number + 1} de {pagina.totalPages}
                </span>
                <button
                  onClick={() =>
                    setNumeroPagina((p) => Math.min(pagina.totalPages - 1, p + 1))
                  }
                  disabled={pagina.number >= pagina.totalPages - 1}
                  className="text-sm text-ledger font-medium disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  Próxima
                </button>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}
