import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { isAxiosError } from "axios";
import { login } from "../api/auth";
import { useAuth } from "../contexts/AuthContext";
import type { ApiError } from "../types/api-error";

const loginSchema = z.object({
  email: z.string().email("Informe um email válido"),
  senha: z.string().min(1, "A senha é obrigatória"),
});

type LoginForm = z.infer<typeof loginSchema>;

export default function Login() {
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [erroApi, setErroApi] = useState<string | null>(null);

  const successMessage = (location.state as { registrado?: boolean } | null)
    ?.registrado
    ? "Conta criada com sucesso. Faça login para continuar."
    : null;

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
  });

  async function onSubmit(dados: LoginForm) {
    setErroApi(null);
    try {
      const resposta = await login(dados);
      signIn(resposta.token);
      navigate("/dashboard");
    } catch (error) {
      if (isAxiosError<ApiError>(error) && error.response) {
        setErroApi(error.response.data.message);
      } else {
        setErroApi("Não foi possível entrar. Tente novamente.");
      }
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-paper px-4">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <span className="font-display text-3xl text-ledger">Finance App</span>
          <p className="text-sm text-ink/60 mt-1">Entre para ver seu resumo financeiro</p>
        </div>

        <div className="bg-white border border-line rounded-lg shadow-sm p-8">
          {successMessage && (
            <div className="mb-4 rounded-md bg-ledger/10 border border-ledger/20 px-3 py-2 text-sm text-ledger">
              {successMessage}
            </div>
          )}

          {erroApi && (
            <div className="mb-4 rounded-md bg-danger/10 border border-danger/20 px-3 py-2 text-sm text-danger">
              {erroApi}
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-ink mb-1">
                Email
              </label>
              <input
                id="email"
                type="email"
                autoComplete="email"
                className="w-full rounded-md border border-line px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ledger"
                {...register("email")}
              />
              {errors.email && (
                <p className="mt-1 text-xs text-danger">{errors.email.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="senha" className="block text-sm font-medium text-ink mb-1">
                Senha
              </label>
              <input
                id="senha"
                type="password"
                autoComplete="current-password"
                className="w-full rounded-md border border-line px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ledger"
                {...register("senha")}
              />
              {errors.senha && (
                <p className="mt-1 text-xs text-danger">{errors.senha.message}</p>
              )}
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full rounded-md bg-ledger text-white py-2.5 text-sm font-medium hover:bg-ledger-light transition-colors disabled:opacity-60"
            >
              {isSubmitting ? "Entrando..." : "Entrar"}
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-ink/60">
            Ainda não tem conta?{" "}
            <Link to="/registro" className="text-ledger font-medium hover:underline">
              Cadastre-se
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
