import client from "./client";
import type {
  CriarUsuarioRequest,
  UsuarioResponse,
  LoginRequest,
  LoginResponse,
} from "../types/auth";

export async function registrar(
  dados: CriarUsuarioRequest
): Promise<UsuarioResponse> {
  const response = await client.post<UsuarioResponse>("/auth/register", dados);
  return response.data;
}

export async function login(dados: LoginRequest): Promise<LoginResponse> {
  const response = await client.post<LoginResponse>("/auth/login", dados);
  return response.data;
}
