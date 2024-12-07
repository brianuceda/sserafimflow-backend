# PostgreSQL Database Configuration
export SF_POSTGRES_DB=
export POSTGRES_USER=
export POSTGRES_PASSWORD=

# AWS Configuration
export AWS_ACCESS_KEY=
export AWS_SECRET_KEY=
export AWS_REGION=
export SF_S3_BUCKET_NAME=

# JWT Configuration
export JWT_SECRET_KEY=
export JWT_EXP_TIME=

---

## Tasas de Cambio
`¿Qué le está pasando a la moneda principal?`
- PEN -> USD: **salePrice**
- USD -> PEN: **purchasePrice**

## Bancos

### Banco 1: Banco de Crédito del Perú
```json
{
  "realName": "Banco de Crédito del Perú",
  "ruc": "20234567891",
  "username": "bcp@bancos.com",
  "password": "Password_1_Banco_BCP",
  "nominalRate": 0.10,
  "effectiveRate": 0.12,
}
{
  "username": "bcp@bancos.com",
  "password": "Password_1_Banco_BCP"
}
```

### Banco 2: Interbank
```json
{
  "realName": "Interbank",
  "ruc": "20345678912",
  "username": "interbank@bancos.com",
  "password": "Password_1_Banco_Interbank",
  "nominalRate": 0.09,
  "effectiveRate": 0.11,
}
{
  "username": "interbank@bancos.com",
  "password": "Password_1_Banco_Interbank"
}
```

## Empresas

### Empresa 1: Grupo CINTE
```json
{
  "realName": "Grupo CINTE",
  "ruc": "20563198169",
  "username": "grupo_cinte@empresas.com",
  "password": "Password_1_Empresa_Grupo_Cinte",
}
{
  "username": "grupo_cinte@empresas.com",
  "password": "Password_1_Empresa_Grupo_Cinte"
}
```

### Empresa 2: Baufest
```json
{
  "realName": "Baufest",
  "ruc": "20604077789",
  "username": "baufest@empresas.com",
  "password": "Password_1_Empresa_Baufest",
}
{
  "username": "baufest@empresas.com",
  "password": "Password_1_Empresa_Baufest"
}
```