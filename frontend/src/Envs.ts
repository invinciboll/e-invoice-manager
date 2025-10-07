export const backendUrl =
    import.meta.env.VITE_BACKEND_HOST && import.meta.env.VITE_BACKEND_PORT
        ? `https://${import.meta.env.VITE_BACKEND_HOST
        }`
        : "http://localhost:4711";
