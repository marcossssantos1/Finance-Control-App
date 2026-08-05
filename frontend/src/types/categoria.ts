export type TipoCategoria = "RECEITA" | "DESPESA";

export interface Categoria {
  id: number;
  nome: string;
  tipo: TipoCategoria;
  cor: string | null;
  padrao: boolean;
  dataCriacao: string;
}
