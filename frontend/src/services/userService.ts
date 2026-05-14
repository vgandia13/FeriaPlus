import { UsuarioResponseDTO } from '@/types/UsuarioResponseDTO';
import { DashboardDTO } from '@/types/DashboardDTO';
import api from './apiService';

export const userService = {
  getLoggedUser: async (): Promise<UsuarioResponseDTO> => {
    const response = await api.get('/perfil/me');
    return response.data;
  },
  getDashboardData: async (): Promise<DashboardDTO> => {
    const response = await api.get('/perfil/dashboard');
    return response.data;
  },
};
