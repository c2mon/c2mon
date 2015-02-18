<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<c2mon:template title="${title}">
  <div class="row">
    <div class="col-lg-12">
      <ul class="breadcrumb">
        <li>
          <a href="../../">Home</a>
          <span class="divider"></span>
        </li>
        <li>${title}</li>
      </ul>

      <div class="page-header">
        <h1>${title}</h1>
      </div>

      <div class="alert alert-info">
        <strong>${instruction}</strong>
      </div>

      <div class="alert alert-danger">
        id: <strong> ${err} </strong> could not be found.
      </div>

      <form class="well form-inline" action="" method="post">
        <input class="form-control" style="display: inline" type="text" name="id" value="${formTagValue}" />
        <input class="btn btn-large btn-primary" type="submit" value="Submit">
      </form>
    </div>
  </div>
  <!--/row-->
</c2mon:template>
