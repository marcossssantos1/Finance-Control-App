import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  ResponsiveContainer,
  type PieLabelRenderProps,
} from "recharts";
import type { ResumoCategoria } from "../types/dashboard";
import { formatarMoeda } from "../utils/formato";

interface GraficoCategoriasProps {
  dados: ResumoCategoria[];
}

const PALETA = ["#1f3d33", "#c99a3b", "#b3432b", "#2f5a4b", "#8a6d1f", "#7a2c1c"];

export default function GraficoCategorias({ dados }: GraficoCategoriasProps) {
  const despesas = dados.filter((item) => item.tipo === "DESPESA");

  if (despesas.length === 0) {
    return (
      <div className="bg-white border border-line rounded-lg shadow-sm p-5 flex items-center justify-center h-64">
        <p className="text-sm text-ink/60">
          Nenhuma despesa no período selecionado
        </p>
      </div>
    );
  }

  return (
    <div className="bg-white border border-line rounded-lg shadow-sm p-5">
      <p className="text-sm font-medium text-ink mb-3">Despesas por categoria</p>
      <ResponsiveContainer width="100%" height={280}>
        <PieChart>
          <Pie
            data={despesas}
            dataKey="total"
            nameKey="categoriaNome"
            cx="50%"
            cy="50%"
            outerRadius={90}
            label={(entry: PieLabelRenderProps) =>
              (entry as unknown as ResumoCategoria).categoriaNome ?? ""
            }
          >
            {despesas.map((entrada, index) => (
              <Cell
                key={entrada.categoriaId}
                fill={PALETA[index % PALETA.length]}
              />
            ))}
          </Pie>
          <Tooltip
            formatter={(value) => formatarMoeda(Number(value))}
          />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
