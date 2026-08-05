import { useState } from "react";

type Periodo = "mesAtual" | "mesAnterior" | "customizado";

interface SeletorPeriodoProps {
  periodo: Periodo;
  onChange: (periodo: Periodo, dataInicio: string, dataFim: string) => void;
}

const OPCOES: { valor: Periodo; label: string }[] = [
  { valor: "mesAtual", label: "Mês atual" },
  { valor: "mesAnterior", label: "Mês anterior" },
  { valor: "customizado", label: "Customizado" },
];

export default function SeletorPeriodo({ periodo, onChange }: SeletorPeriodoProps) {
  const [dataInicioCustom, setDataInicioCustom] = useState("");
  const [dataFimCustom, setDataFimCustom] = useState("");

  function selecionarOpcao(opcao: Periodo) {
    if (opcao === "customizado") {
      // aguarda o usuário preencher as datas antes de disparar onChange
      onChange(opcao, dataInicioCustom, dataFimCustom);
      return;
    }
    onChange(opcao, "", "");
  }

  function atualizarDataInicio(valor: string) {
    setDataInicioCustom(valor);
    if (valor && dataFimCustom) {
      onChange("customizado", valor, dataFimCustom);
    }
  }

  function atualizarDataFim(valor: string) {
    setDataFimCustom(valor);
    if (dataInicioCustom && valor) {
      onChange("customizado", dataInicioCustom, valor);
    }
  }

  return (
    <div className="flex flex-col sm:flex-row sm:items-center gap-3">
      <div className="inline-flex rounded-md border border-line bg-white p-1 self-start">
        {OPCOES.map((opcao) => (
          <button
            key={opcao.valor}
            type="button"
            onClick={() => selecionarOpcao(opcao.valor)}
            className={`px-3 py-1.5 text-sm rounded transition-colors ${
              periodo === opcao.valor
                ? "bg-ledger text-white"
                : "text-ink/70 hover:bg-paper"
            }`}
          >
            {opcao.label}
          </button>
        ))}
      </div>

      {periodo === "customizado" && (
        <div className="flex items-center gap-2">
          <input
            type="date"
            value={dataInicioCustom}
            onChange={(e) => atualizarDataInicio(e.target.value)}
            className="rounded-md border border-line px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-ledger"
          />
          <span className="text-ink/50 text-sm">até</span>
          <input
            type="date"
            value={dataFimCustom}
            onChange={(e) => atualizarDataFim(e.target.value)}
            className="rounded-md border border-line px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-ledger"
          />
        </div>
      )}
    </div>
  );
}
