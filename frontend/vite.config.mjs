import { defineConfig } from 'vite'

export default defineConfig({
    define: { 'process.env.NODE_ENV': JSON.stringify('production') },
    build: {
        outDir: '../src/main/resources/static/react-app',
        emptyOutDir: true,
        lib: {
            entry: 'src/main.jsx',
            formats: ['es'],
            fileName: () => 'react-crud.js'
        }
    }
})