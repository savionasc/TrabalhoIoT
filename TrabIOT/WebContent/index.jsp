<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%br.ufc.quixada.controller.PrincipalController.main(null);%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<a href="mostrarMensagem">Hello World!</a>listarFuncionarios
<a href="listarFuncionarios">Listar Funcionarios!</a>
<%
    String mensagem = "Bem vindo!";
%>

<% out.println(mensagem); %>
<%= mensagem %><br />
 

</body>
</html>