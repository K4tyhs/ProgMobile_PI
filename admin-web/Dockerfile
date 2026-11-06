# Build do painel e servidor Nginx (alternativa à Vercel).
# docker build --build-arg VITE_API_URL=https://api.exemplo.org -t proxima-etapa-admin .
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
ARG VITE_API_URL
ARG VITE_ROUTER=browser
RUN npm test && npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
