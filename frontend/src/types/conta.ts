export type TipoConta = "CORRENTE" | "POUPANCA" | "CARTEIRA" | "INVESTIMENTO";

export interface Conta {
  id: number;
  nome: string;
  tipo: TipoConta;
  saldoInicial: number;
  dataCriacao: string;
}

export interface SaldoConta {
  contaId: number;
  contaNome: string;
  saldoAtual: number;
}
