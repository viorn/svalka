import ky from './lib/ky.js';

new Vue({
  el: '#filesTable',
  data() {
    return {
      files: null,
      currentDir: null
    };
  },
  methods: {
      clickToFile: function (file) {
        if (file.type=="FOLDER") {
            if (file.path!="") {
                this.currentDir = "/"+file.path
            } else {
                this.currentDir = ""
            }
        } else {
            window.open("/api/files/get/"+file.path, '_blank').focus();
        }
      }
    },
    watch: {
            'currentDir': function(val, oldVal){
                ky.get("/api/files/get"+val)
                    .then(response => response.json())
                    .then(json => {
                        json.sort((a,b)=>(a.type < b.type))
                        if (val!=""){
                            console.log("val: "+val)
                            var s = val.substring(1).split('/')
                            s.pop()
                            var path = ""
                            if(s!="/") {
                                path = s.join("/")
                            }
                            json.splice(0, 0, {
                                name: "...",
                                path: path,
                                type: "FOLDER"
                            })
                        }
                        this.files = json
                    })
          }
        },
  mounted() {
    this.currentDir = ""
  }
});