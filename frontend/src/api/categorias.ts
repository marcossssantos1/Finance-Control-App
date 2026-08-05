import client from "./client";
import type { Categoria, TipoCategoria } from "../types/categoria";

export async function listarCategorias(): Promise<Categoria[]> {
  const response = await client.get<Categoria[]>("/categorias");
  return response.data;
}

export async function criarCategoria(dados: {
  nome: string;
  tipo: TipoCategoria;
  cor?: string;
}): Promise<Categoria> {
  const response = await client.post<Categoria>("/categorias", dados);
  return response.data;
}
