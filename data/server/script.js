var samplequeries = [
    "Who created the comic Captain America?",
        "How high is the Mount Everest?",
        "List the children of Margaret Thatcher.",
        "When did Michael Jackson die?",
        "What is the time zone of Salt Lake City?",
        "Who is the husband of Amanda Palmer?",
        "Who founded Intel?",
        "Who developed Minecraft?",
        "What is the birth name of Angela Merkel ?",
        "Give me all movies directed by Francis Ford Coppola.",
        "Who was the father of Queen Elizabeth II?",
        "What is the capital of Canada ?"
];

$(document).ready(function() {
    var samplequery = samplequeries[Math.floor(Math.random() * samplequeries.length)];
    $("#querytext").attr("placeholder", samplequery);
    $("#queryform").submit(function() {
        var query = $("#querytext").val();
        if (!query || query == "") {
            query = $("#querytext").attr("placeholder");
        }
        $.post("query", query, function(result) {
            handleResult(JSON.parse(result));
        });
    });
    $("#errormsg").hide();
    function handleResult(json) {
        var $result = $("#result");
        $result.html("");
        if (!json.result) {
            $("#errormsg").show();
            return;
        } else {
            $("#errormsg").hide();
        }
        var data = json.data;
        for (var i = 0; i < data.length; i++) {
            var item = data[i];
            var div = $("<div></div>");
            var label = item.label;
            var uri = item.uri;
            if (!label) {
                label = uri;
            }
            if (!uri) {
                div.html(label);
            } else {
                var a = $("<a></a>");
                a.attr("href", uri);
                a.attr("target", "_blank");
                a.html(label);
                a.appendTo(div);
            }
            div.appendTo($result);
        }
    }

    // test
    // handleResult({"result":"1","data":[{"label":"Lolo Soetoro","uri":"http://www.wikidata.org/entity/Q4115068"},{"label":"Barack Obama, Sr.","uri":"http://www.wikidata.org/entity/Q649593"}]});
});
 