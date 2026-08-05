import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { isAxiosError } from "axios";
import NavBar from "../components/NavBar";
import { listarCategorias, criarCategoria } from "../api/categorias";
import type { Categoria } from "../types/categoria";
import type { ApiError } from "../types/api-error";

const categoriaSchema = z.object({
  nome: z.string().min(1, "O nome é obrigatório"),
  tipo: z.enum(["RECEITA", "DESPESA"]),
  cor: z.string().optional(),
});

type CategoriaForm = z.infer<typeof categoriaSchema>;

function ListaCategorias({ titulo, categorias }: { titulo: string; categorias: Categoria[] }) {
  return (
    <div>
      <p className="text-sm font-medium text-ink mb-3">{titulo}</p>
      {categorias.length === 0 ? (
        <p className="text-sm text-ink/60">Nenhuma categoria aqui ainda.</p>
      ) : (
        <div className="flex flex-wrap gap-2">
          {categorias.map((categoria) => (
            <span
              key={categoria.id}
              className="inline-flex items-center gap-2 rounded-full border border-line bg-white px-3 py-1.5 text-sm"
            >
              <span
                className="w-2.5 h-2.5 rounded-full"
                style={{ backgroundColor: categoria.cor ?? "#1f3d33" }}
              />
              {categoria.nome}
              <span
                className={
                  categoria.tipo === "RECEITA" ? "text-ledger" : "text-danger"
                }
              >
                {categoria.tipo === "RECEITA" ? "receita" : "despesa"}
              </span>
            </span>
          ))}
        </div>
      )}
    </div>
  );
}

export default function Categorias() {
  const [categorias, setCategorias] = useState<Categoria[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [erroForm, setErroForm] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<CategoriaForm>({
    resolver: zodResolver(categoriaSchema),
    defaultValues: { tipo: "DESPESA", cor: "#1f3d33" },
  });

  async function carregarCategorias() {
    setCarregando(true);
    setErro(null);
    try {
      const resposta = await listarCategorias();
      setCategorias(resposta);
    } catch (error) {
      if (isAxiosError<ApiError>(error) && error.response) {
        setErro(error.response.data.message);
      } else {
        setErro("Não foi possível carregar as categorias.");
      }
    } finally {
      setCarregando(false);
    }
  }

  useEffect(() => {
    carregarCategorias();
  }, []);

  async function onSubmit(dados: CategoriaForm) {
    setErroForm(null);
    try {
      const novaCategoria = await criarCategoria(dados);
      setCategorias((atual) => [...atual, novaCategoria]);
      reset({ nome: "", tipo: "DESPESA", cor: "#1f3d33" });
    } catch (error) {
      if (isAxiosError<ApiError>(error) && error.response) {
        setErroForm(error.response.data.message);
      } else {
        setErroForm("Não foi possível criar a categoria. Tente novamente.");
      }
    }
  }

  const padrao = categorias.filter((c) => c.padrao);
  const minhas = categorias.filter((c) => !c.padrao);

  return (
    <div className="min-h-screen bg-paper">
      <NavBar />

      <main className="max-w-3xl mx-auto px-4 py-6 space-y-6">
        <h1 className="font-display text-2xl text-ledger">Categorias</h1>

        <div className="bg-white border border-line rounded-lg shadow-sm p-6">
          <h2 className="text-sm font-medium text-ink mb-4">Nova categoria</h2>

          {erroForm && (
            <div className="mb-4 rounded-md bg-danger/10 border border-danger/20 px-3 py-2 text-sm text-danger">
              {erroForm}
            </div>
          )}

          <form
            onSubmit={handleSubmit(onSubmit)}
            className="grid grid-cols-1 sm:grid-cols-3 gap-4 items-start"
            noValidate
          >
            <div>
              <label htmlFor="nome" className="block text-sm font-medium text-ink mb-1">
                Nome
              </label>
              <input
                id="nome"
                type="text"
                className="w-full rounded-md border border-line px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ledger"
                {...register("nome")}
              />
              {errors.nome && (
                <p className="mt-1 text-xs text-danger">{errors.nome.message}</p>
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
              <label htmlFor="cor" className="block text-sm font-medium text-ink mb-1">
                Cor
              </label>
              <input
                id="cor"
                type="color"
                className="w-full h-9 rounded-md border border-line px-1 py-1"
                {...register("cor")}
              />
            </div>

            <div className="sm:col-span-3">
              <button
                type="submit"
                disabled={isSubmitting}
                className="rounded-md bg-ledger text-white px-4 py-2 text-sm font-medium hover:bg-ledger-light transition-colors disabled:opacity-60"
              >
                {isSubmitting ? "Criando..." : "Criar categoria"}
              </button>
            </div>
          </form>
        </div>

        {erro && (
          <div className="rounded-md bg-danger/10 border border-danger/20 px-3 py-2 text-sm text-danger">
            {erro}
          </div>
        )}

        {carregando ? (
          <div className="h-16 rounded-lg border border-line bg-white animate-pulse" />
        ) : (
          <div className="space-y-6">
            <ListaCategorias titulo="Padrão do sistema" categorias={padrao} />
            <ListaCategorias titulo="Minhas categorias" categorias={minhas} />
          </div>
        )}
      </main>
    </div>
  );
}
