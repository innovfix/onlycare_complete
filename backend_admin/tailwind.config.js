/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./resources/**/*.blade.php",
    "./resources/**/*.js",
    "./resources/**/*.vue",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        'dark-bg': '#000000',
        'dark-surface': '#121212',
        'dark-border': '#2A2A2A',
        'dark-text': '#FFFFFF',
        'dark-text-secondary': '#A0A0A0',
        'primary': '#FF1744', // Pink
        'primary-dark': '#D50000',
        'secondary': '#2196F3', // Blue
        'success': '#00E676',
        'warning': '#FFC400',
        'danger': '#FF1744',
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}

