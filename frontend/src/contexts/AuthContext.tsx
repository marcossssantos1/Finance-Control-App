import {
  createContext,
  useContext,
  useState,
  type ReactNode,
} from "react";

const TOKEN_STORAGE_KEY = "finance_app_token";

interface AuthContextValue {
  token: string | null;
  isAuthenticated: boolean;
  signIn: (token: string) => void;
  signOut: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() =>
    localStorage.getItem(TOKEN_STORAGE_KEY)
  );

  function signIn(newToken: string) {
    localStorage.setItem(TOKEN_STORAGE_KEY, newToken);
    setToken(newToken);
  }

  function signOut() {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    setToken(null);
    window.location.href = "/login";
  }

  return (
    <AuthContext.Provider
      value={{ token, isAuthenticated: !!token, signIn, signOut }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth deve ser usado dentro de um AuthProvider");
  }
  return context;
}
