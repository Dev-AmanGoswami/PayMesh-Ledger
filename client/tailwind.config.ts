import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./app/**/*.{ts,tsx}", "./components/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        // Authentic Google / Google Pay palette
        gblue: "#1a73e8",
        gblueDark: "#1765cc",
        ggreen: "#1e8e3e",
        gred: "#d93025",
        gyellow: "#f9ab00",
        gsurface: "#f1f3f4", // Google light-grey surface
        gtext: "#202124",
        gmuted: "#5f6368",
      },
      fontFamily: {
        sans: ["Google Sans", "Roboto", "Arial", "sans-serif"],
      },
      boxShadow: {
        gcard: "0 1px 2px rgba(60,64,67,0.3), 0 1px 3px 1px rgba(60,64,67,0.15)",
        glow: "0 1px 3px rgba(60,64,67,0.3), 0 4px 8px 3px rgba(60,64,67,0.15)",
      },
      backgroundImage: {
        "brand-gradient": "linear-gradient(135deg,#4285f4 0%,#1a73e8 100%)",
      },
    },
  },
  plugins: [],
};

export default config;
