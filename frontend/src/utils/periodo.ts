interface Periodo {
  dataInicio: string;
  dataFim: string;
}

function formatarData(data: Date): string {
  const ano = data.getFullYear();
  const mes = String(data.getMonth() + 1).padStart(2, "0");
  const dia = String(data.getDate()).padStart(2, "0");
  return `${ano}-${mes}-${dia}`;
}

function primeiroEUltimoDiaDoMes(ano: number, mes: number): Periodo {
  const primeiroDia = new Date(ano, mes, 1);
  const ultimoDia = new Date(ano, mes + 1, 0);
  return {
    dataInicio: formatarData(primeiroDia),
    dataFim: formatarData(ultimoDia),
  };
}

export function obterMesAtual(): Periodo {
  const hoje = new Date();
  return primeiroEUltimoDiaDoMes(hoje.getFullYear(), hoje.getMonth());
}

export function obterMesAnterior(): Periodo {
  const hoje = new Date();
  const mesAnterior = new Date(hoje.getFullYear(), hoje.getMonth() - 1, 1);
  return primeiroEUltimoDiaDoMes(
    mesAnterior.getFullYear(),
    mesAnterior.getMonth()
  );
}
