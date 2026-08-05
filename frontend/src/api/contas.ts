import client from "./client";
import type { Conta, SaldoConta, TipoConta } from "../types/conta";

export async function listarContas(): Promise<Conta[]> {
  const response = await client.get<Conta[]>("/contas");
  return response.data;
}

export async function buscarSaldoConta(contaId: number): Promise<SaldoConta> {
  const response = await client.get<SaldoConta>(`/contas/${contaId}/saldo`);
  return response.data;
}

export async function criarConta(dados: {
  nome: string;
  tipo: TipoConta;
  saldoInicial?: number;
}): Promise<Conta> {
  const response = await client.post<Conta>("/contas", dados);
  return response.data;
}
