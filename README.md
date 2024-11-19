
# Projeto de Estoque - POC

Este projeto é uma prova de conceito (POC) para a gestão de estoque. Ele foi desenvolvido como parte de um projeto acadêmico, com o objetivo de gerenciar produtos, suas quantidades e gerar relatórios em PDF. 

## Índice

- [Introdução](#introdução)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Instalação e Configuração](#instalação-e-configuração)
- [Como Executar o Projeto](#como-executar-o-projeto)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Contribuição](#contribuição)
- [Autores](#autores)
- [Licença](#licença)

## Introdução

Este projeto visa facilitar a gestão de produtos em estoque, com funcionalidades como cadastro de novos produtos, remoção, adição de quantidade e exportação de relatórios em PDF. Ele foi desenvolvido com tecnologias modernas e melhores práticas de programação para garantir uma interface amigável e um back-end robusto.

## Tecnologias Utilizadas

- **Front-end**:
  - **HTML (69.5%)**: Utilizado para a estruturação das páginas.
  - **CSS (0.3%)**: Para a estilização das páginas.
  - **JavaScript**: Adiciona interatividade e dinamismo.
  - **Bootstrap**: Framework utilizado para design responsivo e componentes prontos.

- **Back-end**:
  - **Java (30.2%)**: Linguagem de programação principal para a lógica de negócios.
  - **Thymeleaf**: Mecanismo de template para renderização das páginas dinâmicas.
  - **H2 Database**: Banco de dados embutido, ideal para desenvolvimento e pequenas aplicações.

## Instalação e Configuração

1. **Clone o repositório para sua máquina local**:
   
   git clone https://github.com/seu-usuario/projeto-estoque.git
   cd projeto-estoque/estoque-poc
   

3. **Configure o projeto**:
   - Certifique-se de ter o Java JDK 11 (ou versão compatível) instalado.
   - Verifique se o Maven está instalado e configurado corretamente em seu sistema.

4. **Compilar o projeto**:
   - Navegue até o diretório raiz do projeto e execute o comando:
  
     mvn clean install
     
   - Isso fará a compilação do projeto e baixará todas as dependências necessárias.

## Como Executar o Projeto

1. **Execute o projeto com Maven**:

   mvn spring-boot:run


   - Ou, execute o arquivo `.jar` gerado na pasta `target`:

     java -jar target/nome-do-arquivo.jar


2. **Acesse a aplicação**:
   - A aplicação estará disponível em:

     http://localhost:8080


3. **Console do Banco de Dados H2**:
   - Acesse o console H2 em:

     http://localhost:8080/h2-console

   - Utilize as credenciais configuradas no `application.properties` para acessar.

## Contribuição

Contribuições são bem-vindas! Para contribuir com este projeto:

1. Faça um fork do projeto.
2. Crie uma branch para sua funcionalidade/bugfix (`git checkout -b minha-funcionalidade`).
3. Commit suas alterações (`git commit -m 'Adicionei uma nova funcionalidade'`).
4. Faça o push para a branch (`git push origin minha-funcionalidade`).
5. Abra um Pull Request.

## Autores

- **Janaina Conceicao Tavares da Silva** - *Desenvolvedora principal* - [GitHub](https://github.com/tavaresjana))
- **Matheus Mauricio** - *Designer*
- **Cezar Navoscone Roman** - *Documentação* - [GitHub](https://github.com/aRandomITguy)
- **Sebastião Barbosa Pereira Junior** - *Documentação*
- **Ricardo Pereira Rodrigues** - *Coffe Drinker*


## Agradecimentos

- Aos professores e colegas que apoiaram durante o desenvolvimento do projeto.
- Recursos online e comunidades de desenvolvedores que ajudaram na solução de problemas.
