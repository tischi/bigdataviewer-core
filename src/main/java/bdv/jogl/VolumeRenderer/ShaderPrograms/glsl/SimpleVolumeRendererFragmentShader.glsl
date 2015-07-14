#version 130
//http://www.visualizationlibrary.org/documentation/pag_guide_raycast_volume.html
uniform sampler3D inVolumeTexture;
uniform sampler1D inColorTexture;
out vec4 fragmentColor;
smooth in vec3 textureCoord;
uniform float inMaxVolumeValue;
uniform float inMinVolumeValue;
uniform vec3 inEyePosition;
float val_threshold =1;

void main(void)
{	
	

	//float volume = textureCoord.x; //texture(inVolumeTexture,textureCoord ).x ;
	//fragmentColor  = vec4(textureCoord,1);
	//fragmentColor = vec4(texture(inVolumeTexture,textureCoord ).r/ inMaxVolumeValue,0.1f,0.1f ,1.f);//vec4(textureCoord,1.f);
	//fragmentColor = vec4(1.f,1.f,1.f,1.f);

	const float sample_step =0.05f;
	 const float brightness = 50.0f;
    // NOTE: ray direction goes from frag_position to eye_position, i.e. back to front
    vec3 ray_dir = normalize(inEyePosition - textureCoord);
    vec3 ray_pos = textureCoord; // the current ray position
    vec3 pos111 = vec3(1.0, 1.0, 1.0);
    vec3 pos000 = vec3(0.0, 0.0, 0.0);

    fragmentColor = vec4(0.0, 0.0, 0.0, 0.0);
    vec4 color;
    do
    {
        // note: 
        // - ray_dir * sample_step can be precomputed
        // - we assume the volume has a cube-like shape

        ray_pos += ray_dir * sample_step;

        // break out if ray reached the end of the cube.
        if (any(greaterThan(ray_pos,pos111)))
            break;

        if (any(lessThan(ray_pos,pos000)))
            break;

        float density = (texture3D(inVolumeTexture, ray_pos).r-inMinVolumeValue) / (inMaxVolumeValue-inMinVolumeValue);

        color.rgb = vec3(1f,1f,1f); //texture1D(trfunc_texunit, density).rgb;
        color.a   = density * sample_step * val_threshold * brightness;
        fragmentColor.rgb = fragmentColor.rgb * (1.0 - color.a) + color.rgb * color.a;
    }
    while(true);
	if(fragmentColor ==vec4(0.0, 0.0, 0.0, 0.0)){
	   fragmentColor = vec4(1.0, 0.0, 0.0, 1.0);
	   discard;
	}
}