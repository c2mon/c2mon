<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<c2mon:template title="${title}">
  <div class="row">
    <div class="col-lg-12">
      <ul class="breadcrumb">
        <li>
          <a href="../">Home</a>
          <span class="divider"></span>
        </li>
        <li>${title}</li>
      </ul>

      <div class="page-header">
        <h1>${title}</h1>
      </div>

      <c:url var="submitUrl" value="${formSubmitUrl}" />

      <form class="well form-inline" action="${submitUrl}" method="post">

        <select name="id" class="form-control">
          <c:forEach items="${processNames}" var="processName">
            <option>${processName}</option>
          </c:forEach>
        </select>
        <input class="btn btn-large btn-primary" type="submit" value="Submit">
      </form>

    </div>
  </div>
  <!--/row-->
</c2mon:template>

