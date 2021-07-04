<%@ page import="model.ristorante.Ristorante" %>
<%@ page import="model.tipologia.Tipologia" %>
<%@ page import="java.util.StringJoiner" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <jsp:include page="../partials/head.jsp">
        <jsp:param name="title" value="Menu"/>
        <jsp:param name="styles" value="info_ris_crm"/>
        <jsp:param name="scripts" value="info_ris_crm"/>
    </jsp:include>
</head>
<body>
<style>

    .index {
        padding: 1rem;<%--dimensione relativa al root--%>
        background-color: white;
        border-radius: 10px;
        opacity: revert;
    }

    .index > * {
        margin: 10px;
    }

    input:focus {
        outline: 1px solid var(--primary);
    }

    input {
        height: 40px;
        line-height: 40px;
    }

    #urlImmagine {
        height: 50px;
        line-height: 27px;
    }

    .info{
        text-align: center;
        font-style: normal;
        font-weight: bold;
        padding: 1rem;
        border-top: 1px solid black;
        background-color: lightgrey;
    }

    .info > a {
        text-decoration: none;
        color: black;
        margin-right: 25px;
        margin-left: 25px;
    }


    .table{
        border-collapse: collapse;
        background-color:white;
        font-size: large;
        font-weight: normal;
        font-style: normal;
        font-family: Myriad;
    }

    .table > thead{
        visibility: hidden;
    }

    .table tr{
        border:none;
        margin: .5rem;
        padding: .5rem;
        display:block;
    }

    .table > tbody{
        border:1px solid black;
        border-radius: 20px;
    }

    .table > tbody td{
        display: block;
        border-bottom: 1px solid black;
        text-align: right;
        padding: .5rem;
    }

    .table > tbody td:before{
        content: attr(data-head);
        float: left;
        font-weight: bold;
        text-transform: uppercase;
    }

    .container{
        margin:10px;
    }

    div#header{
        background-color: rgba(0,0,0,0);
        background-image: url("/FoodOut/covers/${ristorante.urlImmagine}");
        background-size: cover;
        background-repeat: no-repeat;
        background-position: center center;
        height: 500px;
        position: relative;
    }

    .search {
        background-color: rgba(0,0,0,0.53);
        border-radius: 5px;
        max-height: 500px;
        padding: 10px;
        margin-right: 5px;
    }

    .search .tipologia, .search .fitro{
        color: white;
        background-color: var(--primary);
        font-weight: bold;
    }

    .search .title{
        color: white;
    }

    .tipologia, .fitro, .prodotto, .menu {
        padding: 10px;
        border-radius: 5px;
        border: 1px solid black;
        margin:2px;
        max-height: 100px;

    }

    .prodotto, .menu {
        color: black;
        background-color: lightgrey;
    }

    .title{
        text-align: center;
        padding: 5px;
        height: content-box;
    }

    #sconto {
        height: auto;
        margin-right: 5px;
    }

    #sconto:focus {
        outline: none;
    }

    #links{
        background-color: var(--primary);
        padding:3px;
        border:1px solid var(--primary);
        border-radius: 10px;
        margin:2px;
    }
    #links a{
        text-decoration: none;
        color:white;
        font-family:Myriad;
        font-weight:bold;
        font-style:normal;
        margin:5px;
    }



</style>
<div class="app">
    <div class="cell grid-x" id="header">
        <nav class="grid-y navbar align-center cell">
            <img src="/FoodOut/images/logo.png" class="fluid-image" id="logo">
            <div id="links">
                <a href="${pageContext.request.contextPath}/ristorante/show-info-admin?id=${ristorante.codice}"> Info </a>
                <a href="${pageContext.request.contextPath}/ristorante/show-menu-admin?id=${ristorante.codice}"/> Menu </a>
                <a href="${pageContext.request.contextPath}/ristorante/show-recensioni?id=${ristorante.codice}"> Recensioni </a>
            </div>
        </nav>
        <section class="app grid-x container">
            <form class="cell w20 grid-x search">
                <section class="grid-x cell w100">
                    <h3 class="cell w100 title" style="color:white"> Tipologie: </h3>
                    <div class="tipologie cell grid-x">
                        <c:forEach items="${ristorante.tipologie}" var="tipologia">
                            <label class="field cell w100 tipologia" >
                                <span style="font-style: italic"> ${tipologia.nome} </span>
                            </label>
                        </c:forEach>
                    </div>
                    <h3 class="cell w100 title"> Filtri: </h3>
                    <div class="cell grid-x align-center fitro">
                        <input type="checkbox" id="sconto" name="sconto" value="1">
                        <label for="sconto"> Sconto </label>
                    </div>
                </section>
            </form>
            <div class="cell w75 grid-x justify-center show-menu">
                <div class="grid-x justify-center info-ris cell">
                    <fieldset class="grid-x cell w100 index">
                        <h2 class="cell"> Prodotti </h2>
                        <c:forEach items="${ristorante.prodotti}" var="prodotto">
                        <label class="field cell w100 prodotto" >
                            <span style="font-weight: bold"> ${prodotto.nome} </span>
                        </label>
                        </c:forEach>
                        <h2 class="cell"> Menu </h2>
                        <c:forEach items="${menus}" var="menu">
                            <label class="field cell w100 menu" >
                                <span style="font-weight: bold"> ${menu.nome} </span>
                            </label>
                        </c:forEach>
                    </fieldset>
                </div>
            </div>

    </div>
</div>

</body>
</html>
