export const backendUrl =
    import.meta.env.VITE_BACKEND_HOST && import.meta.env.VITE_BACKEND_PORT
        ? `http://${import.meta.env.VITE_BACKEND_HOST}:${import.meta.env.VITE_BACKEND_PORT
        }`
        : "http://localhost:4711";
