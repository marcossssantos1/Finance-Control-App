export function formatarMoeda(valor: number): string {
  return valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

export function formatarData(data: string): string {
  // recebe yyyy-MM-dd e evita o bug de fuso do `new Date("yyyy-MM-dd")`
  // (que interpreta como UTC e pode exibir o dia anterior)
  const [ano, mes, dia] = data.split("-");
  return `${dia}/${mes}/${ano}`;
}
