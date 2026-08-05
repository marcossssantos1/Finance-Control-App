import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
import { buscarResumo } from "../api/dashboard";
import { listarContas, buscarSaldoConta } from "../api/contas";
import { obterMesAtual, obterMesAnterior } from "../utils/periodo";
import { formatarMoeda } from "../utils/formato";
import CardResumo from "../components/CardResumo";
import GraficoCategorias from "../components/GraficoCategorias";
import SeletorPeriodo from "../components/SeletorPeriodo";
import NavBar from "../components/NavBar";
import type { DashboardResumo } from "../types/dashboard";
import type { SaldoConta } from "../types/conta";
import type { ApiError } from "../types/api-error";

type Periodo = "mesAtual" | "mesAnterior" | "customizado";

export default function Dashboard() {
  const mesAtual = obterMesAtual();
  const [periodo, setPeriodo] = useState<Periodo>("mesAtual");
  const [dataInicio, setDataInicio] = useState(mesAtual.dataInicio);
  const [dataFim, setDataFim] = useState(mesAtual.dataFim);

  const [resumo, setResumo] = useState<DashboardResumo | null>(null);
  const [saldos, setSaldos] = useState<SaldoConta[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  function handleMudarPeriodo(
    novoPeriodo: Periodo,
    novaDataInicio: string,
    novaDataFim: string
  ) {
    setPeriodo(novoPeriodo);

    if (novoPeriodo === "mesAtual") {
      const { dataInicio, dataFim } = obterMesAtual();
      setDataInicio(dataInicio);
      setDataFim(dataFim);
    } else if (novoPeriodo === "mesAnterior") {
      const { dataInicio, dataFim } = obterMesAnterior();
      setDataInicio(dataInicio);
      setDataFim(dataFim);
    } else if (novaDataInicio && novaDataFim) {
      setDataInicio(novaDataInicio);
      setDataFim(novaDataFim);
    }
  }

  useEffect(() => {
    // no modo customizado, só busca quando as duas datas já estão preenchidas
    if (periodo === "customizado" && (!dataInicio || !dataFim)) {
      return;
    }

    let cancelado = false;

    async function carregarDados() {
      setCarregando(true);
      setErro(null);
      try {
        const [resumoResposta, contas] = await Promise.all([
          buscarResumo(dataInicio, dataFim),
          listarContas(),
        ]);

        const saldosResposta = await Promise.all(
          contas.map((conta) => buscarSaldoConta(conta.id))
        );

        if (!cancelado) {
          setResumo(resumoResposta);
          setSaldos(saldosResposta);
        }
      } catch (error) {
        if (cancelado) return;
        if (isAxiosError<ApiError>(error) && error.response) {
          setErro(error.response.data.message);
        } else {
          setErro("Não foi possível carregar o dashboard. Tente novamente.");
        }
      } finally {
        if (!cancelado) setCarregando(false);
      }
    }

    carregarDados();

    return () => {
      cancelado = true;
    };
  }, [dataInicio, dataFim, periodo]);

  return (
    <div className="min-h-screen bg-paper">
      <NavBar />

      <main className="max-w-5xl mx-auto px-4 py-6 space-y-6">
        <SeletorPeriodo periodo={periodo} onChange={handleMudarPeriodo} />

        {erro && (
          <div className="rounded-md bg-danger/10 border border-danger/20 px-3 py-2 text-sm text-danger">
            {erro}
          </div>
        )}

        {carregando ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {[1, 2, 3].map((i) => (
              <div
                key={i}
                className="h-24 rounded-lg border border-line bg-white animate-pulse"
              />
            ))}
          </div>
        ) : (
          resumo && (
            <>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <CardResumo
                  titulo="Receitas do período"
                  valor={resumo.totalReceitas}
                  variante="receita"
                />
                <CardResumo
                  titulo="Despesas do período"
                  valor={resumo.totalDespesas}
                  variante="despesa"
                />
                <CardResumo
                  titulo="Saldo do período"
                  valor={resumo.saldoPeriodo}
                  variante="saldo"
                />
              </div>

              <GraficoCategorias dados={resumo.porCategoria} />

              <div>
                <p className="text-sm font-medium text-ink mb-3">Contas</p>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  {saldos.map((saldo) => (
                    <div
                      key={saldo.contaId}
                      className="bg-white border border-line rounded-lg shadow-sm p-5"
                    >
                      <p className="text-sm text-ink/60 mb-1">{saldo.contaNome}</p>
                      <p className="font-display text-xl text-ledger">
                        {formatarMoeda(saldo.saldoAtual)}
                      </p>
                    </div>
                  ))}
                  {saldos.length === 0 && (
                    <p className="text-sm text-ink/60">
                      Nenhuma conta cadastrada ainda.
                    </p>
                  )}
                </div>
              </div>
            </>
          )
        )}
      </main>
    </div>
  );
}
