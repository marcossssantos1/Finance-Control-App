import client from "./client";
import type {
  Transacao,
  PageTransacoes,
  FiltroTransacoes,
} from "../types/transacao";
import type { TipoCategoria } from "../types/categoria";

export async function listarTransacoes(
  filtro: FiltroTransacoes
): Promise<PageTransacoes> {
  const params: Record<string, string | number> = {};
  if (filtro.dataInicio) params.dataInicio = filtro.dataInicio;
  if (filtro.dataFim) params.dataFim = filtro.dataFim;
  if (filtro.contaId !== undefined) params.contaId = filtro.contaId;
  if (filtro.categoriaId !== undefined) params.categoriaId = filtro.categoriaId;
  if (filtro.page !== undefined) params.page = filtro.page;
  if (filtro.size !== undefined) params.size = filtro.size;

  const response = await client.get<PageTransacoes>("/transacoes", { params });
  return response.data;
}

export async function criarTransacao(dados: {
  descricao: string;
  valor: number;
  tipo: TipoCategoria;
  contaId: number;
  categoriaId: number;
  dataTransacao: string;
}): Promise<Transacao> {
  const response = await client.post<Transacao>("/transacoes", dados);
  return response.data;
}

export async function excluirTransacao(id: number): Promise<void> {
  await client.delete(`/transacoes/${id}`);
}
