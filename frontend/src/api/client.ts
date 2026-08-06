import axios, { AxiosHeaders } from "axios";

const TOKEN_STORAGE_KEY = "finance_app_token";

const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY);
  if (token) {
    // Versões recentes do axios usam a classe AxiosHeaders, que exige
    // .set() em vez de atribuição direta de propriedade — atribuição
    // direta pode silenciosamente não anexar o header. Garantimos que
    // config.headers seja uma instância válida e usamos o método correto.
    if (!(config.headers instanceof AxiosHeaders)) {
      config.headers = AxiosHeaders.from(config.headers ?? {});
    }
    config.headers.set("Authorization", `Bearer ${token}`);
  }
  return config;
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default client;