import { formatarMoeda } from "../utils/formato";

interface CardResumoProps {
  titulo: string;
  valor: number;
  variante: "receita" | "despesa" | "saldo";
}

export default function CardResumo({ titulo, valor, variante }: CardResumoProps) {
  const valorFormatado = formatarMoeda(valor);

  const corValor =
    variante === "despesa" || (variante === "saldo" && valor < 0)
      ? "text-danger"
      : "text-ledger";

  return (
    <div className="bg-white border border-line rounded-lg shadow-sm p-5">
      <p className="text-sm text-ink/60 mb-1">{titulo}</p>
      <p className={`font-display text-2xl ${corValor}`}>{valorFormatado}</p>
    </div>
  );
}
