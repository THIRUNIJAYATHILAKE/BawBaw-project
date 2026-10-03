import axios, { AxiosInstance, AxiosError } from "axios";
import AsyncStorage from "@react-native-async-storage/async-storage";

const API_BASE_URL =
  process.env.REACT_APP_API_URL || "http://localhost:8080/api";

interface AuthResponse {
  userId: number;
  username: string;
  email: string;
  fullName: string;
  role: string;
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      timeout: 30000,
      headers: {
        "Content-Type": "application/json",
      },
    });

    // Add request interceptor
    this.client.interceptors.request.use(
      async (config) => {
        const token = await AsyncStorage.getItem("accessToken");
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error),
    );

    // Add response interceptor for token refresh
    this.client.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        const originalRequest = error.config as any;

        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;

          try {
            const refreshToken = await AsyncStorage.getItem("refreshToken");
            if (refreshToken) {
              const response = await this.refreshAccessToken(refreshToken);
              await AsyncStorage.setItem("accessToken", response.accessToken);
              await AsyncStorage.setItem("refreshToken", response.refreshToken);

              originalRequest.headers.Authorization = `Bearer ${response.accessToken}`;
              return this.client(originalRequest);
            }
          } catch (refreshError) {
            await this.clearAuthTokens();
            return Promise.reject(refreshError);
          }
        }

        return Promise.reject(error);
      },
    );
  }

  // Auth endpoints
  async register(data: any) {
    return this.client.post<AuthResponse>("/auth/register", data);
  }

  async login(username: string, password: string) {
    return this.client.post<AuthResponse>("/auth/login", {
      username,
      password,
    });
  }

  async refreshAccessToken(refreshToken: string) {
    const response = await this.client.post<AuthResponse>(
      "/auth/refresh-token",
      {
        refreshToken,
      },
    );
    return response.data;
  }

  async forgotPassword(email: string) {
    return this.client.post("/auth/forgot-password", { email });
  }

  async resetPassword(token: string, newPassword: string) {
    return this.client.post("/auth/reset-password", { token, newPassword });
  }

  async logout() {
    await this.clearAuthTokens();
  }

  // User endpoints
  async getUserProfile() {
    return this.client.get("/users/profile");
  }

  async updateUserProfile(data: any) {
    return this.client.put("/users/profile", data);
  }

  async changePassword(oldPassword: string, newPassword: string) {
    return this.client.post("/users/change-password", {
      oldPassword,
      newPassword,
    });
  }

  // Pet endpoints
  async getPets() {
    return this.client.get("/owner/pets");
  }

  async getPetById(petId: number) {
    return this.client.get(`/owner/pets/${petId}`);
  }

  async createPet(data: any) {
    return this.client.post("/owner/pets", data);
  }

  async updatePet(petId: number, data: any) {
    return this.client.put(`/owner/pets/${petId}`, data);
  }

  async deletePet(petId: number) {
    return this.client.delete(`/owner/pets/${petId}`);
  }

  // Appointment endpoints
  async getAppointments() {
    return this.client.get("/owner/appointments");
  }

  async bookAppointment(data: any) {
    return this.client.post("/owner/appointments", data);
  }

  async cancelAppointment(appointmentId: number, reason: string) {
    return this.client.post(`/owner/appointments/${appointmentId}/cancel`, {
      reason,
    });
  }

  // Medical Records endpoints
  async getMedicalRecords(petId: number) {
    return this.client.get(`/owner/pets/${petId}/medical-records`);
  }

  async getVaccinations(petId: number) {
    return this.client.get(`/owner/pets/${petId}/vaccinations`);
  }

  async getDewormingRecords(petId: number) {
    return this.client.get(`/owner/pets/${petId}/deworming`);
  }

  // Helper methods
  private async clearAuthTokens() {
    await AsyncStorage.removeItem("accessToken");
    await AsyncStorage.removeItem("refreshToken");
    await AsyncStorage.removeItem("user");
  }

  getClient() {
    return this.client;
  }
}

export const apiClient = new ApiClient();
export default apiClient;
