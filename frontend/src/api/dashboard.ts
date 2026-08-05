import client from "./client";
import type { DashboardResumo } from "../types/dashboard";

export async function buscarResumo(
  dataInicio?: string,
  dataFim?: string
): Promise<DashboardResumo> {
  const params: Record<string, string> = {};
  if (dataInicio) params.dataInicio = dataInicio;
  if (dataFim) params.dataFim = dataFim;

  const response = await client.get<DashboardResumo>("/dashboard/resumo", {
    params,
  });
  return response.data;
}
