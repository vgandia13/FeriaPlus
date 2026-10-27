import axios from 'axios';
import { toast } from 'sonner';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

let _setIsLogged: ((v: boolean) => void) | null = null;
let _setUsuario: ((u: null) => void) | null = null;

export function setAuthCallbacks(
  setIsLogged: (v: boolean) => void,
  setUsuario: (u: null) => void,
) {
  _setIsLogged = setIsLogged;
  _setUsuario = setUsuario;
}

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    toast.error('Error en la solicitud');
    return Promise.reject(error);
  },
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const esAuth = error.config?.url?.startsWith('/auth');
    if (status === 401 && !esAuth) {
      localStorage.removeItem('token');
      localStorage.removeItem('usuario');
      _setIsLogged?.(false);
      _setUsuario?.(null);
      window.location.href = '/login';
    } else if (status !== 401) {
      toast.error('Ha ocurrido un error. Intentelo de nuevo');
    }
    return Promise.reject(error);
  },
);

export default api;
