package bdv.export.n5;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;

import java.util.List;

public class S3Demo
{

	public static final String CBB_BIGDATA = "cbb-bigdata";

	public static void main( String[] args )
	{
		final AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration("https://s3.embl.de", "us-west-2");

		final BasicAWSCredentials credentials = new BasicAWSCredentials( CBB_BIGDATA, "UZUTutgnW7" );

		final AmazonS3 s3 = AmazonS3ClientBuilder
				.standard()
				.withPathStyleAccessEnabled(true)
				.withEndpointConfiguration(endpoint)
				.withCredentials(new AWSStaticCredentialsProvider(credentials))
				.build();

		final boolean b = s3.doesBucketExistV2( "cbb-bigdata" );

		final S3Object object = s3.getObject( "cbb-bigdata", "mri-stack-n5s3.xml" );

		final ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
				.withBucketName(CBB_BIGDATA)
				.withPrefix("mri-stack-n5s3.xml")
				.withMaxKeys(1);
		final ListObjectsV2Result objectsListing = s3.listObjectsV2(listObjectsRequest);
		System.out.println( objectsListing.getKeyCount() );
		//final List< Bucket > buckets = s3.listBuckets();
	}
}
