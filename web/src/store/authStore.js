import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export const useAuthStore = create(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      usuario: null,
      setAuth: ({ accessToken, refreshToken, role, nome }) =>
        set({ accessToken, refreshToken, usuario: { role, nome } }),
      clearAuth: () => set({ accessToken: null, refreshToken: null, usuario: null }),
    }),
    { name: 'unipsi-auth' },
  ),
)
