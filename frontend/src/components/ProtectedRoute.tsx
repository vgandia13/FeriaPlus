import { Navigate, Outlet } from "react-router-dom";
import { useData } from "../contexts/AppContext";

const ProtectedRoute = () => {
  const { isLogged } = useData();

  if (!isLogged) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
