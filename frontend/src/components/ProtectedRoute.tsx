import { Navigate, Outlet } from "react-router-dom";
import { useData } from "../contexts/AppContext";
import { Rol } from "../types/Rol";

interface Props {
  allowedRoles?: Rol[];
}

const ProtectedRoute = ({ allowedRoles }: Props) => {
  const { isLogged, usuario } = useData();

  if (!isLogged) {
    return <Navigate to="/login" replace />;
  }

  if(allowedRoles && usuario && !allowedRoles.includes(usuario.rol)) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
