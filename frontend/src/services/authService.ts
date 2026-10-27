import api from './apiService';
import type { LoginRequest } from '../types/LoginRequest';
import type { AuthResponse } from '../types/AuthResponse';
import { UsuarioRegistro } from '@/types/UsuarioRegistro';
import type { UsuarioResponseDTO } from '@/types/UsuarioResponseDTO';

export const AuthService = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/login', credentials);
    const jsonResponse = response.data;
    if (jsonResponse.token) {
      localStorage.setItem('token', jsonResponse.token);
      localStorage.setItem('usuario', JSON.stringify(jsonResponse.usuario));
    }
    return jsonResponse;
  },

  register: async (
    credentials: UsuarioRegistro,
  ): Promise<UsuarioResponseDTO> => {
    const response = await api.post<UsuarioResponseDTO>(
      '/auth/register',
      credentials,
    );
    return response.data;
  },
};
