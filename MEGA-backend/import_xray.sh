#!/bin/bash

# ================= CONFIGURAÇÃO =================
# 1. Vai ao Jira > Apps > Manage Apps > Xray > API Keys para gerar isto:
CLIENT_ID="24472AEB67F649329C48A668A952FA8A"
CLIENT_SECRET="b2cd076794c33ed98e3740a987a3655c03998c7fa1c15e77636fffa4c590f883"

# 2. A Chave do teu projeto (Ex: MEGA, SPORTS, TQS)
PROJECT_KEY="MEGA"
# ================================================

echo "1. A pedir token de autenticação..."

# Pede um token temporário à API do Xray
token=$(curl -s -H "Content-Type: application/json" \
     -X POST \
     --data "{ \"client_id\": \"$CLIENT_ID\",\"client_secret\": \"$CLIENT_SECRET\" }" \
     https://xray.cloud.getxray.app/api/v2/authenticate)

# Remove as aspas do token (limpeza)
token=$(echo $token | tr -d '"')

echo "Token obtido com sucesso!"
echo "2. A enviar resultados de testes para o projeto $PROJECT_KEY..."

# Envia TODOS os ficheiros XML gerados pelo Maven na pasta target
# O Xray vai criar uma 'Test Execution' nova com estes resultados
for file in target/surefire-reports/*.xml; do
    if [ -f "$file" ]; then
        echo "A enviar: $file"
        curl -H "Content-Type: text/xml" \
             -X POST \
             -H "Authorization: Bearer $token" \
             --data @"$file" \
             "https://xray.cloud.getxray.app/api/v2/import/execution/junit?projectKey=$PROJECT_KEY"
        echo -e "\nDone."
    fi
done

echo "Processo concluído! Vai ao Jira ver a nova Test Execution."