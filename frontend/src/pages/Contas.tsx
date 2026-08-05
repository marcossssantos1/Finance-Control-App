import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { isAxiosError } from "axios";
import NavBar from "../components/NavBar";
import { listarContas, criarConta } from "../api/contas";
import { formatarMoeda } from "../utils/formato";
import type { Conta, TipoConta } from "../types/conta";
import type { ApiError } from "../types/api-error";

const TIPOS: { valor: TipoConta; label: string }[] = [
  { valor: "CORRENTE", label: "Conta corrente" },
  { valor: "POUPANCA", label: "Poupança" },
  { valor: "CARTEIRA", label: "Carteira" },
  { valor: "INVESTIMENTO", label: "Investimento" },
];

const contaSchema = z.object({
  nome: z.string().min(1, "O nome é obrigatório"),
  tipo: z.enum(["CORRENTE", "POUPANCA", "CARTEIRA", "INVESTIMENTO"]),
  saldoInicial: z.coerce.number().min(0, "O saldo inicial não pode ser negativo").optional(),
});

type ContaFormInput = z.input<typeof contaSchema>;
type ContaForm = z.output<typeof contaSchema>;

export default function Contas() {
  const [contas, setContas] = useState<Conta[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [erroForm, setErroForm] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ContaFormInput, unknown, ContaForm>({
    resolver: zodResolver(contaSchema),
    defaultValues: { tipo: "CORRENTE" },
  });

  async function carregarContas() {
    setCarregando(true);
    setErro(null);
    try {
      const resposta = await listarContas();
      setContas(resposta);
    } catch (error) {
      if (isAxiosError<ApiError>(error) && error.response) {
        setErro(error.response.data.message);
      } else {
        setErro("Não foi possível carregar as contas.");
      }
    } finally {
      setCarregando(false);
    }
  }

  useEffect(() => {
    carregarContas();
  }, []);

  async function onSubmit(dados: ContaForm) {
    setErroForm(null);
    try {
      const novaConta = await criarConta(dados);
      setContas((atual) => [...atual, novaConta]);
      reset({ nome: "", tipo: "CORRENTE", saldoInicial: undefined });
    } catch (error) {
      if (isAxiosError<ApiError>(error) && error.response) {
        setErroForm(error.response.data.message);
      } else {
        setErroForm("Não foi possível criar a conta. Tente novamente.");
      }
    }
  }

  return (
    <div className="min-h-screen bg-paper">
      <NavBar />

      <main className="max-w-3xl mx-auto px-4 py-6 space-y-6">
        <h1 className="font-display text-2xl text-ledger">Contas</h1>

        <div className="bg-white border border-line rounded-lg shadow-sm p-6">
          <h2 className="text-sm font-medium text-ink mb-4">Nova conta</h2>

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
                {TIPOS.map((tipo) => (
                  <option key={tipo.valor} value={tipo.valor}>
                    {tipo.label}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label htmlFor="saldoInicial" className="block text-sm font-medium text-ink mb-1">
                Saldo inicial
              </label>
              <input
                id="saldoInicial"
                type="number"
                step="0.01"
                min="0"
                placeholder="0,00"
                className="w-full rounded-md border border-line px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ledger"
                {...register("saldoInicial")}
              />
              {errors.saldoInicial && (
                <p className="mt-1 text-xs text-danger">{errors.saldoInicial.message}</p>
              )}
            </div>

            <div className="sm:col-span-3">
              <button
                type="submit"
                disabled={isSubmitting}
                className="rounded-md bg-ledger text-white px-4 py-2 text-sm font-medium hover:bg-ledger-light transition-colors disabled:opacity-60"
              >
                {isSubmitting ? "Criando..." : "Criar conta"}
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
          <div className="space-y-2">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-16 rounded-lg border border-line bg-white animate-pulse" />
            ))}
          </div>
        ) : (
          <div className="space-y-2">
            {contas.map((conta) => (
              <div
                key={conta.id}
                className="bg-white border border-line rounded-lg shadow-sm p-4 flex items-center justify-between"
              >
                <div>
                  <p className="text-sm font-medium text-ink">{conta.nome}</p>
                  <p className="text-xs text-ink/60">
                    {TIPOS.find((t) => t.valor === conta.tipo)?.label ?? conta.tipo}
                  </p>
                </div>
                <p className="text-sm text-ledger font-display">
                  {formatarMoeda(conta.saldoInicial)}
                </p>
              </div>
            ))}
            {contas.length === 0 && (
              <p className="text-sm text-ink/60">Nenhuma conta cadastrada ainda.</p>
            )}
          </div>
        )}
      </main>
    </div>
  );
}
