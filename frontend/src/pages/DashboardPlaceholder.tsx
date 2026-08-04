import { useAuth } from "../contexts/AuthContext";

export default function DashboardPlaceholder() {
  const { signOut } = useAuth();

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-paper px-4">
      <span className="font-display text-3xl text-ledger mb-2">Finance App</span>
      <p className="text-ink/70 mb-6">Dashboard em breve</p>
      <button
        onClick={signOut}
        className="text-sm text-ledger font-medium hover:underline"
      >
        Sair
      </button>
    </div>
  );
}
