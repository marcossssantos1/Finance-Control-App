import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./contexts/AuthContext";
import ProtectedRoute from "./routes/ProtectedRoute";
import Login from "./pages/Login";
import Registro from "./pages/Registro";
import DashboardPlaceholder from "./pages/DashboardPlaceholder";

function RotaInicial() {
  const { isAuthenticated } = useAuth();
  return <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />;
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/registro" element={<Registro />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<DashboardPlaceholder />} />
          </Route>

          <Route path="/" element={<RotaInicial />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
