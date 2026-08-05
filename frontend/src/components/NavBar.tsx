import { NavLink } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

const LINKS = [
  { to: "/dashboard", label: "Dashboard" },
  { to: "/contas", label: "Contas" },
  { to: "/categorias", label: "Categorias" },
  { to: "/transacoes", label: "Transações" },
];

export default function NavBar() {
  const { signOut } = useAuth();

  return (
    <header className="border-b border-line bg-white">
      <div className="max-w-5xl mx-auto px-4 py-4 flex items-center justify-between gap-4">
        <div className="flex items-center gap-6 min-w-0">
          <span className="font-display text-xl text-ledger shrink-0">
            Finance App
          </span>
          <nav className="hidden sm:flex items-center gap-4">
            {LINKS.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                className={({ isActive }) =>
                  `text-sm font-medium ${
                    isActive ? "text-ledger" : "text-ink/60 hover:text-ledger"
                  }`
                }
              >
                {link.label}
              </NavLink>
            ))}
          </nav>
        </div>
        <button
          onClick={signOut}
          className="text-sm text-ink/70 hover:text-ledger font-medium shrink-0"
        >
          Sair
        </button>
      </div>
      <nav className="sm:hidden flex items-center gap-4 px-4 pb-3 overflow-x-auto">
        {LINKS.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            className={({ isActive }) =>
              `text-sm font-medium whitespace-nowrap ${
                isActive ? "text-ledger" : "text-ink/60"
              }`
            }
          >
            {link.label}
          </NavLink>
        ))}
      </nav>
    </header>
  );
}
