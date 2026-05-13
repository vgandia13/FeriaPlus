import { useData } from "@/contexts/AppContext";
import { setAuthCallbacks } from "@/services/apiService";
import { useEffect } from "react";

export function AuthInitializer() {
  const { setIsLogged, setUsuario } = useData();

  useEffect(() => {
    setAuthCallbacks(setIsLogged, setUsuario);
  }, [setIsLogged, setUsuario]);

  return null;
}
