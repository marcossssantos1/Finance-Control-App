export type TipoCategoria = "RECEITA" | "DESPESA";

export interface ResumoCategoria {
  categoriaId: number;
  categoriaNome: string;
  tipo: TipoCategoria;
  total: number;
}

export interface DashboardResumo {
  totalReceitas: number;
  totalDespesas: number;
  saldoPeriodo: number;
  porCategoria: ResumoCategoria[];
}
