// The root URL for the RESTful services
var rootURL = "rest/devices";

var currentdevice;

// Retrieve device list when application starts 
findAll();

// Register listeners
$('#slider1').live('slidestop', function(){
    var slider_value = $(this).val()
    console.log("slider Volume" + slider_value)
    updateDevice(currentdevice.identifier,'VOL',slider_value,true);
});

$('#slider2').live('slidestop', function(){
    var slider_value = $(this).val()
    console.log("slider Balance " + slider_value)
    updateDevice(currentdevice.identifier,'BAL',slider_value,true);
});

$('#toggleswitch1').live('slidestop', function(){
    var slider_value = $(this).val();
    var totransport  ='';
    if(slider_value == 'on')
        totransport = 'ON';
    else
        totransport = 'OFF';
    console.log("slider Mute " + totransport);
    updateDevice(currentdevice.identifier,'MUTE',totransport,false);
});

// Replace broken images with generic device bottle
$("img").error(function()
{
    console.log("Error in finding picture");
    $(this).attr("src", "images/generic.jpg");

});

function findAll() {
    console.log('findAll');
    $.ajax({
        type: 'GET',
        url: rootURL,
        dataType: "json", // data type of response
        success: renderList
    });
}

function findByName(searchKey) {
    console.log('findByName: ' + searchKey);
    $.ajax({
        type: 'GET',
        url: rootURL + '/search/' + searchKey,
        dataType: "json",
        success: renderList
    });
}

function findById(id) {
    console.log('findById: ' + id);
    $.ajax({
        type: 'GET',
        url: rootURL + '/' + id,
        dataType: "json",
        success: function(data){
            console.log('findById success: ' + data.name);
            currentdevice = data;
            renderDetails(currentdevice);
        }
    });
}



function updateDevice(deviceID,key,value,iseql) {
    deviceID = currentdevice.identifier;
    console.log("updateDevice: " + deviceID)
    $.ajax({
        type: 'PUT',
        contentType: 'application/json',
        url: rootURL + '/' + deviceID,
        dataType: "json",
        data: JSON.stringify({
            "deviceIdentifier": deviceID == "" ? null : deviceID,
            "key": key,
            "value": value ,
            "iseql": iseql
        }),
        success: function(data, textStatus, jqXHR){
            alert('device updated successfully');
        },
        error: function(jqXHR, textStatus, errorThrown){
            alert('update device error: ' + textStatus);
        }
    });
}

    function renderList(data) {
    // JAX-RS serializes an empty list as null, and a 'collection of one' as an object (not an 'array of one')
    var list = data == null ? [] : (data instanceof Array ? data : [data]);


        $('#deviceList div').remove();
    $.each(list, function(index, device) {
        // yes, that is not nice and will not work with more than one device, FIXIT
        currentdevice = device;

        $('#deviceList').append('<div data-role="collapsible"  data-theme="b" data-content-theme="d"><h4>' + device.identifier + '</h4>'+
            '<div class="ui-grid-b">'+
            '<div class="ui-block-a">ID</div>'+
        '<div class="ui-block-b" id="deviceID">' + device.identifier + '</div>'+
        '<div class="ui-block-a">Counter Power</div>'+
        '<div class="ui-block-b" id="devicePower">' + device.power + '</div>'+
        '<div class="ui-block-a">Counter Mains</div>'+
        '<div class="ui-block-b" id="deviceMain">' + device.mains + '</div>'+
            '<div class="ui-block-a">Version Hardware</div>'+
            '<div class="ui-block-b" id="deviceHW">' + device.versionHW + '</div>'+
            '<div class="ui-block-a">Version Software</div>'+
            '<div class="ui-block-b" id="deviceSW">' + device.versionSW + '</div>'+
            '<div class="ui-block-a">Mute</div>'+
            '<div class="ui-block-b" id="deviceMute">' + device.mute + '</div>'+
            '<div class="ui-block-a">Listen</div>'+
            '<div class="ui-block-b" id="deviceListen">' + device.listen + '</div>'+
            '<div class="ui-block-a">Bas</div>'+
            '<div class="ui-block-b" id="deviceBas">' + device.bas + '</div>'+
            '<div class="ui-block-a">Treb</div>'+
            '<div class="ui-block-b" id="deviceTreb">' + device.treb + '</div>'+
            '<div class="ui-block-a">Volume</div>'+
            '<div class="ui-block-b" id="deviceVol">' + device.volume + '</div>'+
            '<div class="ui-block-a">Balance</div>'+
            '<div class="ui-block-b" id="deviceBal">' + device.balance + '</div>'+
            '<div class="ui-block-a">DaisyChain</div>'+
            '<div class="ui-block-b" id="deviceDaisy">' + device.daisyChain + '</div>'+
            '</div>'+

            '</div>');

        $('div[data-role=collapsible]').collapsible({refresh:true});


        if(device.volume != null)
        {

            $('#slider1').val(device.volume).slider({refresh:true});

            console.log("Setting Volume to " + $('#slider1').val());
        }
        if(device.balance != null)
        {
         $('#slider2').val(device.balance).slider({refresh:true});
         console.log("Setting balance to" + $('#slider2').val());
        }

        if(device.listen == 0)
        {
            $("input[value=src0]").attr('checked',true).checkboxradio('refresh');
        }
        else if(device.listen == 1)
        {
            $("input[value=src1]").attr('checked',true).checkboxradio('refresh');
        }
        else if(device.listen == 2)
        {
            $("input[value=src2]").attr('checked',true).checkboxradio('refresh');
        }
        else if(device.listen == 3)
        {
            $("input[value=src3]").attr('checked',true).checkboxradio('refresh');
        }

        if(device.mute =='OFF')
            $('#toggleswitch1').val('off').slider("refresh");
        else
            $('#toggleswitch1').val('on').slider("refresh");
    });
    }
