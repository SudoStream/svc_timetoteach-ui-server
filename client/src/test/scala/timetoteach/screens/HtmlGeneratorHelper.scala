package timetoteach.screens

trait HtmlGeneratorHelper {

  def expectedEmptyValue : String = "" +
    "<div class=\"row dayoftheweek-row align-items-center\">" +
      "<div class=\"col-sm-1 text-primary timetable-dayofweek-header align-self-center\">Mon</div>"  +
    <div class="col-sm-3 "> +
      <div class="row"> +
        <button class="col-12 rounded subject subject-maths">Maths</button> +
        </div>
      </div>
      <div class="col-sm-1 small-column "></div>
      <div class="col-sm-3">
        <div class="row">
          <button class="col-4 rounded subject subject-reading">Reading</button>
          <button class="col-4 rounded subject subject-spelling">Spelling</button>
          <button class="col-4 rounded subject subject-ict">ICT</button>
        </div>
      </div>
      <div class="col-sm-1 small-column"></div>

      <div class="col-sm-3">
        <div class="row">
          <button class="col-12 rounded subject subject-topic">IDL</button>
        </div>
      </div>
    </div>

      <div class="row dayoftheweek-row align-items-center">
        <div class="col-sm-1 text-primary timetable-dayofweek-header align-self-center" >Tue</div>
        <div class="col-sm-3 ">
          <div class="row">
            <button class="col-6 rounded subject subject-maths">Maths</button>
            <button class="col-6 rounded subject subject-physical-education">P.E.</button>
          </div>
        </div>
        <div class="col-sm-1 small-column"></div>
        <div class="col-sm-3">
          <div class="row">
            <button class="col-8 rounded subject subject-writing">Writing</button>
            <button class="col-4 rounded subject subject-music">Music</button>
          </div>
        </div>
        <div class="col-sm-1 small-column"></div>
        <div class="col-sm-3">
          <div class="row">
            <button class="col-12 rounded subject subject-topic">IDL</button>
          </div>
        </div>
      </div>

      <div class="row dayoftheweek-row align-items-center">
        <div class="col-sm-1 text-primary timetable-dayofweek-header align-self-center" >Wed</div>
        <div class="col-sm-3 ">
          <div class="row">
            <button class="col-6 rounded subject subject-reading">Reading</button>
            <button class="col-6 rounded subject subject-spelling">Spelling</button>
          </div>
        </div>
        <div class="col-sm-1 small-column"></div>
        <div class="col-sm-3">
          <div class="row">
            <button class="col-8 rounded subject subject-maths">Maths</button>
            <button class="col-4 rounded subject subject-drama">Drama</button>
          </div>
        </div>
        <div class="col-sm-1 small-column"></div>
        <div class="col-sm-3">
          <div class="row">
            <button class="col-6 rounded subject subject-physical-education">P.E.</button>
            <button class="col-6 rounded subject subject-health">Health</button>
          </div>
        </div>
      </div>

      <div class="row dayoftheweek-row align-items-center">
        <div class="col-sm-1 text-primary timetable-dayofweek-header align-self-center" >Thu</div>
        <div class="col-sm-3 ">
          <div class="row">
            <button class="col-12 rounded subject subject-maths">Maths</button>
          </div>
        </div>
        <div class="col-sm-1 small-column"></div>
        <div class="col-sm-3">
          <div class="row">
            <button class="col-4 rounded subject subject-reading">Reading</button>
            <button class="col-8 rounded subject subject-topic">IDL</button>
          </div>
        </div>
        <div class="col-sm-1 small-column"></div>
        <div class="col-sm-3">
          <div class="row">
            <button class="col-12 rounded teacher-cover subject-teacher-covertime">RCCT : Mr Smith</button>
          </div>
          <div class="row">
            <button class="col-6 rounded subject subject-writing">Writing</button>
            <button class="col-6 rounded subject subject-spelling">Spelling</button>
          </div>
        </div>
      </div>

      <div class="row dayoftheweek-row align-items-center">
        <div class="col-sm-1 text-primary timetable-dayofweek-header align-self-center" >Fri</div>
        <div class="col-sm-3 ">
          <div class="row">
            <button class="col-8 rounded subject subject-spelling">Spelling</button>
            <button class="col-4 rounded subject subject-reading">Reading</button>
          </div>
        </div>
        <div class="col-sm-1 small-column"></div>
        <div class="col-sm-3">
          <div class="row">
            <button class="col-8 rounded subject subject-maths">Maths</button>
            <button class="col-4 rounded subject subject-topic">IDL</button>
          </div>
        </div>
        <div class="col-sm-1 small-column"></div>
        <div class="col-sm-3">
          <div class="row">
            <button class="col-6 rounded subject subject-assembly">Assembly</button>
            <button class="col-6 rounded subject subject-golden-time">Golden Time</button>
          </div>
        </div>
      </div>
  ""
}
