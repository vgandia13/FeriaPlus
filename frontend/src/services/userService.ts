import { UsuarioResponseDTO } from '@/types/UsuarioResponseDTO';
import api from './apiService';

export const userService = {
  getLoggedUser: async (): Promise<UsuarioResponseDTO> => {
    const response = await api.get('/perfil/me');
    return response.data;
  },
};
