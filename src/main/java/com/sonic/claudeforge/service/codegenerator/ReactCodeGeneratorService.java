// src/main/java/com/sonic/claudeforge/service/codegenerator/ReactCodeGeneratorService.java
package com.sonic.claudeforge.service.codegenerator;

import com.sonic.claudeforge.model.ProjectConfig;
import com.sonic.claudeforge.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * React Code Generator Service
 * Handles ReactJS project generation
 */
@Service
public class ReactCodeGeneratorService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReactCodeGeneratorService.class);
    
    private final FileUtils fileUtils;
    
    public ReactCodeGeneratorService(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }
    
    public String generateReactProject(ProjectConfig config) {
        logger.info("Generating React project: {}", config.getProjectName());
        
        String projectPath = config.getFrontendProjectPath();
        
        createReactProjectStructure(projectPath, config);
        generatePackageJson(projectPath, config);
        generateIndexHtml(projectPath, config);
        generateReactApp(projectPath, config);
        generateTailwindConfig(projectPath, config);
        generateViteConfig(projectPath, config);
        
        logger.info("React project generated successfully at: {}", projectPath);
        return projectPath;
    }
    
    private void createReactProjectStructure(String projectPath, ProjectConfig config) {
        // Create React project directories
        String[] directories = {
            "src",
            "src/components",
            "src/pages", 
            "src/hooks",
            "src/services",
            "src/utils",
            "src/styles",
            "src/types",
            "public",
            "public/images"
        };
        
        for (String dir : directories) {
            fileUtils.createDirectory(projectPath + File.separator + dir);
        }
    }
    
    private void generatePackageJson(String projectPath, ProjectConfig config) {
        String projectName = config.getProjectName();
        if (config.getFrontendProjectPath().contains("-web")) {
            projectName = config.getProjectName() + "-web";
        }
        
        String packageJson = String.format("""
            {
              "name": "%s",
              "version": "%s",
              "private": true,
              "type": "module",
              "scripts": {
                "dev": "vite",
                "build": "vite build",
                "preview": "vite preview",
                "lint": "eslint src --ext ts,tsx --report-unused-disable-directives --max-warnings 0",
                "type-check": "tsc --noEmit"
              },
              "dependencies": {
                "react": "^18.2.0",
                "react-dom": "^18.2.0",
                "react-router-dom": "^6.15.0",
                "axios": "^1.5.0",
                "lucide-react": "^0.263.1"
              },
              "devDependencies": {
                "@types/react": "^18.2.15",
                "@types/react-dom": "^18.2.7",
                "@typescript-eslint/eslint-plugin": "^6.0.0",
                "@typescript-eslint/parser": "^6.0.0",
                "@vitejs/plugin-react-swc": "^3.3.2",
                "autoprefixer": "^10.4.15",
                "eslint": "^8.45.0",
                "eslint-plugin-react-hooks": "^4.6.0",
                "eslint-plugin-react-refresh": "^0.4.3",
                "postcss": "^8.4.28",
                "tailwindcss": "^3.3.3",
                "typescript": "^5.0.2",
                "vite": "^4.4.5"
              }
            }
            """, projectName, config.getVersion());
        
        fileUtils.writeFile(projectPath + File.separator + "package.json", packageJson);
    }
    
    private void generateIndexHtml(String projectPath, ProjectConfig config) {
        String indexHtml = String.format("""
            <!doctype html>
            <html lang="en">
              <head>
                <meta charset="UTF-8" />
                <link rel="icon" type="image/svg+xml" href="/vite.svg" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <title>%s</title>
              </head>
              <body>
                <div id="root"></div>
                <script type="module" src="/src/main.tsx"></script>
              </body>
            </html>
            """, config.getProjectName());
        
        fileUtils.writeFile(projectPath + File.separator + "index.html", indexHtml);
    }
    
    private void generateReactApp(String projectPath, ProjectConfig config) {
        // Generate main.tsx
        String mainTsx = """
            // src/main.tsx
            import React from 'react'
            import ReactDOM from 'react-dom/client'
            import App from './App'
            import './styles/index.css'
            
            ReactDOM.createRoot(document.getElementById('root')!).render(
              <React.StrictMode>
                <App />
              </React.StrictMode>,
            )
            """;
        
        // Generate App.tsx
        String appTsx = String.format("""
            // src/App.tsx
            import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
            import HomePage from './pages/HomePage'
            import './styles/App.css'
            
            function App() {
              return (
                <Router>
                  <div className="App">
                    <Routes>
                      <Route path="/" element={<HomePage />} />
                    </Routes>
                  </div>
                </Router>
              )
            }
            
            export default App
            """);
        
        // Generate HomePage.tsx
        String homePageTsx = String.format("""
            // src/pages/HomePage.tsx
            import { useState, useEffect } from 'react'
            import { Users, Database, Zap } from 'lucide-react'
            
            interface ApiStatus {
              status: string
              message: string
            }
            
            export default function HomePage() {
              const [apiStatus, setApiStatus] = useState<ApiStatus | null>(null)
              const [loading, setLoading] = useState(true)
            
              useEffect(() => {
                // Test API connection
                fetch('/api/health')
                  .then(response => response.json())
                  .then(data => {
                    setApiStatus(data)
                    setLoading(false)
                  })
                  .catch(error => {
                    console.error('API Error:', error)
                    setApiStatus({ status: 'error', message: 'Unable to connect to backend' })
                    setLoading(false)
                  })
              }, [])
            
              return (
                <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
                  <div className="container mx-auto px-4 py-16">
                    <div className="text-center mb-16">
                      <h1 className="text-5xl font-bold text-gray-900 mb-4">
                        Welcome to %s
                      </h1>
                      <p className="text-xl text-gray-600 max-w-2xl mx-auto">
                        Your full-stack application is ready! This React frontend is connected to your Spring Boot backend.
                      </p>
                    </div>
            
                    <div className="max-w-4xl mx-auto">
                      {/* API Status Card */}
                      <div className="bg-white rounded-xl shadow-lg p-8 mb-8">
                        <h2 className="text-2xl font-semibold text-gray-900 mb-4 flex items-center">
                          <Database className="mr-3 text-blue-600" />
                          Backend Connection
                        </h2>
                        
                        {loading ? (
                          <div className="flex items-center text-gray-600">
                            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600 mr-2"></div>
                            Checking connection...
                          </div>
                        ) : (
                          <div className={\\`flex items-center \\${apiStatus?.status === 'error' ? 'text-red-600' : 'text-green-600'}\\`}>
                            <div className={\\`w-3 h-3 rounded-full mr-2 \\${apiStatus?.status === 'error' ? 'bg-red-500' : 'bg-green-500'}\\`}></div>
                            {apiStatus?.message || 'Connected successfully'}
                          </div>
                        )}
                      </div>
            
                      {/* Features Grid */}
                      <div className="grid md:grid-cols-2 gap-8">
                        <div className="bg-white rounded-xl shadow-lg p-8">
                          <Users className="w-12 h-12 text-blue-600 mb-4" />
                          <h3 className="text-xl font-semibold text-gray-900 mb-2">React Frontend</h3>
                          <p className="text-gray-600">
                            Modern React 18 with TypeScript, Tailwind CSS, and React Router for navigation.
                          </p>
                          <ul className="mt-4 text-sm text-gray-500">
                            <li>• TypeScript for type safety</li>
                            <li>• Tailwind CSS for styling</li>
                            <li>• Vite for fast development</li>
                            <li>• React Router for navigation</li>
                          </ul>
                        </div>
            
                        <div className="bg-white rounded-xl shadow-lg p-8">
                          <Zap className="w-12 h-12 text-green-600 mb-4" />
                          <h3 className="text-xl font-semibold text-gray-900 mb-2">Spring Boot Backend</h3>
                          <p className="text-gray-600">
                            Robust Spring Boot API with database integration, validation, and error handling.
                          </p>
                          <ul className="mt-4 text-sm text-gray-500">
                            <li>• RESTful API endpoints</li>
                            <li>• Database integration</li>
                            <li>• Global exception handling</li>
                            <li>• Request/response logging</li>
                          </ul>
                        </div>
                      </div>
            
                      {/* Next Steps */}
                      <div className="bg-blue-50 rounded-xl p-8 mt-8">
                        <h3 className="text-xl font-semibold text-gray-900 mb-4">Next Steps</h3>
                        <div className="grid md:grid-cols-2 gap-4 text-sm">
                          <div>
                            <h4 className="font-medium text-gray-900 mb-2">Frontend Development:</h4>
                            <ul className="text-gray-600 space-y-1">
                              <li>• Add more components in src/components</li>
                              <li>• Create new pages in src/pages</li>
                              <li>• Set up API services in src/services</li>
                              <li>• Customize styles in src/styles</li>
                            </ul>
                          </div>
                          <div>
                            <h4 className="font-medium text-gray-900 mb-2">Backend Development:</h4>
                            <ul className="text-gray-600 space-y-1">
                              <li>• Create controllers in web.controller</li>
                              <li>• Add services for business logic</li>
                              <li>• Define entities for data models</li>
                              <li>• Configure database repositories</li>
                            </ul>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              )
            }
            """, config.getProjectName());
        
        // Generate CSS files
        String indexCss = """
            @tailwind base;
            @tailwind components;
            @tailwind utilities;
            
            * {
              margin: 0;
              padding: 0;
              box-sizing: border-box;
            }
            
            body {
              font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
                'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
                sans-serif;
              -webkit-font-smoothing: antialiased;
              -moz-osx-font-smoothing: grayscale;
            }
            
            code {
              font-family: source-code-pro, Menlo, Monaco, Consolas, 'Courier New',
                monospace;
            }
            """;
        
        String appCss = """
            .App {
              text-align: left;
            }
            
            .container {
              max-width: 1200px;
              margin: 0 auto;
              padding: 0 16px;
            }
            
            @media (max-width: 768px) {
              .container {
                padding: 0 12px;
              }
            }
            """;
        
        fileUtils.writeFile(projectPath + "/src/main.tsx", mainTsx);
        fileUtils.writeFile(projectPath + "/src/App.tsx", appTsx);
        fileUtils.writeFile(projectPath + "/src/pages/HomePage.tsx", homePageTsx);
        fileUtils.writeFile(projectPath + "/src/styles/index.css", indexCss);
        fileUtils.writeFile(projectPath + "/src/styles/App.css", appCss);
    }
    
    private void generateTailwindConfig(String projectPath, ProjectConfig config) {
        String tailwindConfig = """
            /** @type {import('tailwindcss').Config} */
            export default {
              content: [
                "./index.html",
                "./src/**/*.{js,ts,jsx,tsx}",
              ],
              theme: {
                extend: {},
              },
              plugins: [],
            }
            """;
        
        String postcssConfig = """
            export default {
              plugins: {
                tailwindcss: {},
                autoprefixer: {},
              },
            }
            """;
        
        fileUtils.writeFile(projectPath + "/tailwind.config.js", tailwindConfig);
        fileUtils.writeFile(projectPath + "/postcss.config.js", postcssConfig);
    }
    
    private void generateViteConfig(String projectPath, ProjectConfig config) {
        String viteConfig = """
            import { defineConfig } from 'vite'
            import react from '@vitejs/plugin-react-swc'
            
            // https://vitejs.dev/config/
            export default defineConfig({
              plugins: [react()],
              server: {
                port: 3000,
                proxy: {
                  '/api': {
                    target: 'http://localhost:8080',
                    changeOrigin: true,
                  },
                },
              },
              build: {
                outDir: 'dist',
                sourcemap: true,
              },
            })
            """;
        
        String tsConfig = """
            {
              "compilerOptions": {
                "target": "ES2020",
                "useDefineForClassFields": true,
                "lib": ["ES2020", "DOM", "DOM.Iterable"],
                "module": "ESNext",
                "skipLibCheck": true,
            
                /* Bundler mode */
                "moduleResolution": "bundler",
                "allowImportingTsExtensions": true,
                "resolveJsonModule": true,
                "isolatedModules": true,
                "noEmit": true,
                "jsx": "react-jsx",
            
                /* Linting */
                "strict": true,
                "noUnusedLocals": true,
                "noUnusedParameters": true,
                "noFallthroughCasesInSwitch": true
              },
              "include": ["src"],
              "references": [{ "path": "./tsconfig.node.json" }]
            }
            """;
        
        String tsConfigNode = """
            {
              "compilerOptions": {
                "composite": true,
                "skipLibCheck": true,
                "module": "ESNext",
                "moduleResolution": "bundler",
                "allowSyntheticDefaultImports": true
              },
              "include": ["vite.config.ts"]
            }
            """;
        
        fileUtils.writeFile(projectPath + "/vite.config.ts", viteConfig);
        fileUtils.writeFile(projectPath + "/tsconfig.json", tsConfig);
        fileUtils.writeFile(projectPath + "/tsconfig.node.json", tsConfigNode);
    }
}