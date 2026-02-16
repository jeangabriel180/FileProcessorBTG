# File Processor

## Visão geral

Este projeto apresenta a análise e correção de um código que processa um arquivo de texto utilizando múltiplas threads.

O objetivo foi identificar problemas de concorrência, performance, gerenciamento de recursos e robustez, e implementar
uma solução correta, determinística e eficiente.

---

# ❌ Problemas identificados no código original

---

## 1️⃣ Leitura do arquivo repetida

### 📍 Onde

A leitura do arquivo ocorre dentro do `executor.submit()`:

```java
executor.submit(() ->{
BufferedReader br = new BufferedReader(new FileReader("data.txt"));
    ...
            });
```

E o `submit` é executado múltiplas vezes:

```java
for(int i = 0;
i< 10;i++){
        executor.

submit(...);
}
```

### ⚠️ Impacto

- O arquivo é lido múltiplas vezes
- Geração de dados duplicados
- Baixa escalabilidade

### ✅ Correção

Ler o arquivo uma única vez e paralelizar apenas o processamento.

---

## 2️⃣ Uso de ArrayList compartilhado entre múltiplas threads (race condition)

### 📍 Onde

```java
private static List<String> lines = new ArrayList<>();
```

E múltiplas threads executando:

```java
lines.add(line.toUpperCase());
```

### ⚠️ Impacto

ArrayList não é thread-safe e pode causar:

- Corrupção interna da estrutura
- Resultados inconsistentes
- Race conditions
- Exceções intermitentes

### ✅ Correção

Substituição por array `String[]`, onde cada thread escreve em um índice exclusivo:

```java
String[] output = new String[n];
```

```java
output[index]=processedLine;
```

Benefícios:

- Thread-safe neste contexto
- Sem necessidade de sincronização
- Melhor performance
- Preservação da ordem

---

## 3️⃣ Falta de espera pela finalização das threads

### 📍 Onde

```java
executor.shutdown();
System.out.

println("Lines processed: "+lines.size());
```

### ⚠️ Impacto

`shutdown()` não bloqueia a execução.

O resultado pode ser:

- parcial
- incorreto
- ou zero

### ✅ Correção

Uso de awaitTermination:

```java
executor.shutdown();

if(!executor.

awaitTermination(5,TimeUnit.MINUTES)){
        executor.

shutdownNow();
    throw new

IllegalStateException("Timeout processing file");
}
```

---

## 4️⃣ Gerenciamento incorreto de recursos

### 📍 Onde

```java
BufferedReader br = new BufferedReader(new FileReader("data.txt"));
br.

close();
```

### ⚠️ Impacto

Pode causar vazamento de recursos e erro:

```
Too many open files
```

### ✅ Correção

Uso de try-with-resources:

```java
try(BufferedReader br = new BufferedReader(new FileReader("data.txt"))){
        ...
        }
```

---

## 5️⃣ Tratamento de erro inadequado dentro das threads

### 📍 Onde

```java
catch(Exception e){
        e.

printStackTrace();
}
```

### ⚠️ Impacto

- Falhas silenciosas
- Resultados inconsistentes
- Falta de controle adequado

### ✅ Correção

Uso de gerenciamento adequado do ExecutorService e falha explícita em caso de timeout.

---

## 6️⃣ Uso incorreto de paralelismo

### 📍 Onde

Múltiplas threads lendo o mesmo arquivo.

### ⚠️ Impacto

- Performance degradada
- Complexidade desnecessária

### ✅ Correção

Arquitetura correta:

```
Leitura sequencial 
        ↓
Processamento paralelo 
```

---

# ✅ Solução implementada

A solução final utiliza:

- Leitura única do arquivo utilizando BufferedReader 
- ExecutorService com thread pool fixo
- Escrita por índice em array (`String[]`) para garantir thread safety e ordem de inserção
- Gerenciamento correto do ciclo de vida das threads

---

# 🔁 Solução alternativa com ParallelStream (Java 21)

Uma abordagem alternativa utiliza recursos modernos do Java, como Parallel Streams:

```java
List<String> result = Files.readAllLines(Path.of("data.txt"))
        .parallelStream()
        .map(String::toUpperCase)
        .toList();
```

### Benefícios

- Código mais simples e legível
- Paralelismo automático utilizando ForkJoinPool
- Thread-safe por design
- Preserva a ordem das linhas

### Trade-offs

- Menor controle sobre o pool de threads
- Usa o ForkJoinPool global
- Não permite controle explícito de timeout

Essa abordagem é recomendada quando simplicidade e legibilidade são prioridades, e não há necessidade de controle fino
do paralelismo.

---

