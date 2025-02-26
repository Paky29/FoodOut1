<%@ page import="model.ristorante.Ristorante" %>
<%@ page import="model.tipologia.Tipologia" %>
<%@ page import="java.util.StringJoiner" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>

    <jsp:include page="../partials/head.jsp">
        <jsp:param name="title" value="Info"/>
        <jsp:param name="styles" value="info_ris_crm"/>
        <jsp:param name="scripts" value="info_ris_crm,update_ris_validation"/>
    </jsp:include>

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

        label > span {
            margin: 5px 5px;
            font-style: italic;
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

        div#header{
            background-color: rgba(0,0,0,0);
            background-image: url("/FoodOut/covers/${ristorante.urlImmagine}");
            background-size: cover;
            background-repeat: no-repeat;
            background-position: center center;
            height: 500px;
            position: relative;
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

</head>
<body>
<div class="app">
    <div class="cell grid-x" id="header">
        <nav class="grid-y navbar align-center cell">
            <img src="/FoodOut/images/logo.png" class="fluid-image" id="logo">
            <div id="links" >
                <a href="${pageContext.request.contextPath}/ristorante/show-info-admin?id=${ristorante.codice}"> Info </a>
                <a href="${pageContext.request.contextPath}/ristorante/show-menu-admin?id=${ristorante.codice}"/> Menu </a>
                <a href="${pageContext.request.contextPath}/ristorante/show-recensioni?id=${ristorante.codice}"> Recensioni </a>
            </div>
        </nav>
        <form class="grid-x justify-center align-center info-ris cell" action="${pageContext.request.contextPath}/ristorante/update" method="post" enctype="multipart/form-data" novalidate>
            <c:if test="${not empty alert}">
                <%@ include file="../partials/alert.jsp"%>
            </c:if>
            <fieldset class="grid-x cell w63 index">
                <h2 class="cell"> Info </h2>
                <label for="nome" class="field cell w80" >
                    <span style="font-weight: bold"> Nome: </span>
                    <input class="cell" type="text" name="nome" id="nome" value="${ristorante.nome}" maxlength="30" pattern="^([a-zA-Z]|\\s|[è,à,ò,ù,ì,À, Ò, È, Ù, Ì]|'){1,30}$" required>
                    </span>
                </label>
                <label class="field cell w40 grid-x">
                    <span style="font-weight: bold" class="field cell w40"> Provincia: </span>
                    <input class="cell" type="text" name="provincia" id="provincia" value="${ristorante.provincia}" maxlength="30" pattern="^([a-zA-Z]|\\s|[è,à,ò,ù,ì,À, Ò, È, Ù, Ì]|'){1,30}$" required>
                    <span style="font-weight: bold" class="field cell w40"> Citta: </span>
                    <input class="cell" type="text" name="citta" id="citta" value="${ristorante.citta}" maxlength="30" pattern="^([a-zA-Z]|\\s|[è,à,ò,ù,ì,À, Ò, È, Ù, Ì]|'){1,30}$" required>                </label>
                <label class="field cell w40 grid-x">
                    <span style="font-weight: bold" class="field cell w75"> Via: </span>
                    <input class="cell" type="text" name="via" id="via" value="${ristorante.via}" maxlength="50" pattern="^(\w|\s|[è,à,ò,ù,ì,À, Ò, È, Ù, Ì]|'|\.){1,50}$" required>
                    <span style="font-weight: bold" class="field cell w25"> Civico: </span>
                    <input class="cell" type="text" name="civico" id="civico" value="${ristorante.civico}" min="1" required>
                </label>
                <label class="field cell w40 grid-x">
                    <span style="font-weight: bold" class="field cell w40"> Spesa minima: </span>
                    <input class="cell" type="text" name="spesaMinima" id="spesaMinima" value="${ristorante.spesaMinima}" step="0.01" min="0" pattern="^[0-9]\d{0,9}(\.\d{1,3})?$" required>
                    <span style="font-weight: bold" class="field cell w40"> Tasso consegna:</span>
                    <input class="cell" type="text" name="tassoConsegna" id="tassoConsegna" value="${ristorante.tassoConsegna}" step="0.01" min="0" pattern="^[0-9]\d{0,9}(\.\d{1,3})?$" required>
                </label>
                <label class="field cell w40 grid-x">
                    <span style="font-weight: bold"> Rating: </span>
                    <input type="text" name="rating" id="rating" value="${ristorante.rating}" readonly>
                    <span style="font-weight: bold" class="cell" style="visibility: hidden"> </span>
                    <input type="text" name="id" id="id" value="${ristorante.codice}" style="visibility: hidden" readonly >
                </label>
                <label for="urlImmagine" class="field w80 cell">
                    <input class="cell" type="file" name="urlImmagine" id="urlImmagine" value="${ristorante.urlImmagine}" accept="image/*">
                </label>
                <label for="tipologie" class="field cell w80">
                    <span style="font-weight: bold"> Tipologie: </span>
                    <textarea name="tipologie" id="tipologie" rows="4" cols="100" readonly><%Ristorante r= (Ristorante) request.getAttribute("ristorante");
                        if(!r.getTipologie().isEmpty()){
                            StringJoiner sj=new StringJoiner(",");
                            for(Tipologia t: r.getTipologie()){
                                sj.add(t.getNome());
                            }
                    %><%=sj.toString()%>
                        <%}else{
                            String str="Non ci sono prodotti, quindi tipologie per il ristorante";
                        %><%=str%>
                        <%}%></textarea>
                </label>
                <label for="info" class="field cell w80">
                    <span style="font-weight: bold"> Info:</span>
                    <textarea rows="4" cols="100" type="text" name="info" id="info" maxlength="200" pattern="^(\\w|\\s|[è,à,ò,ù,ì,À, Ò, È, Ù, Ì]|'|\\.){1,200}$" required> ${ristorante.info}</textarea>
                </label>
                <span class="grid-x cell justify-center">
                <button type="submit" class="btn primary"> Modifica info</button>
                </span>
            </fieldset>
        </form>
        <div class="disponibilita grid-x justify-center align-center cell">
            <section class="grid-y cell w63">
                <table class="table">
                    <tbody>
                    <c:forEach items="${ristorante.giorni}" var="disp">
                        <tr>
                            <c:choose>
                                <c:when test="${disp.oraApertura==disp.oraChiusura}">
                                    <td data-head="${disp.giorno}">CHIUSO </td>
                                </c:when>
                                <c:otherwise>
                                    <td data-head="${disp.giorno}">${disp.oraApertura} - ${disp.oraChiusura} </td>
                                </c:otherwise>
                            </c:choose>
                        </tr>
                    </c:forEach>
                    <tr>
                        <td style="border-bottom: none"><a href="/FoodOut/ristorante/update-disponibilita?id=${ristorante.codice}">Modifica orario</a></td>
                    </tr>
                    </tbody>
                </table>
            </section>
        </div>
    </div>
</div>

</body>
</html>

