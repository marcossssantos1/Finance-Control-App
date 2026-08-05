import type { TipoCategoria } from "./categoria";

export interface Transacao {
  id: number;
  descricao: string;
  valor: number;
  tipo: TipoCategoria;
  contaId: number;
  contaNome: string;
  categoriaId: number;
  categoriaNome: string;
  dataTransacao: string;
  dataCriacao: string;
}

export interface PageTransacoes {
  content: Transacao[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface FiltroTransacoes {
  dataInicio?: string;
  dataFim?: string;
  contaId?: number;
  categoriaId?: number;
  page?: number;
  size?: number;
}
